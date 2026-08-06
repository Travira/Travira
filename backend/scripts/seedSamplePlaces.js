/**
 * Seed 20 approved sample places attributed to Preet (superadmin).
 *
 * Usage (from backend folder, with MONGODB_URI set):
 *   node scripts/seedSamplePlaces.js
 *
 * Options:
 *   --replace   delete existing sample places with the same names first
 */

if (process.env.NODE_ENV !== "production") {
  require("dotenv").config({ path: require("path").join(__dirname, "..", ".env") });
}

const mongoose = require("mongoose");
const path = require("path");
const fs = require("fs");
const Place = require("../models/place");
const User = require("../models/user");

const REPLACE = process.argv.includes("--replace");

async function main() {
  const uri = process.env.MONGODB_URI;
  if (!uri) {
    console.error("❌ Set MONGODB_URI in environment or backend/.env");
    process.exit(1);
  }

  await mongoose.connect(uri, { serverSelectionTimeoutMS: 15000 });
  console.log("✅ Connected to MongoDB");

  let preet = await User.findOne({ email: "preet@travira.app" });
  if (!preet) {
    // fallback: any superadmin
    preet = await User.findOne({ role: "superadmin" });
  }
  if (!preet) {
    console.error("❌ Preet / superadmin user not found. Start the server once to seed Preet, then re-run.");
    process.exit(1);
  }
  console.log(`👤 Using addedBy: ${preet.name} <${preet.email}> (${preet._id})`);

  const file = path.join(__dirname, "..", "data", "samplePlaces.json");
  const samples = JSON.parse(fs.readFileSync(file, "utf8"));
  if (!Array.isArray(samples) || samples.length === 0) {
    console.error("❌ samplePlaces.json is empty");
    process.exit(1);
  }

  const names = samples.map((p) => p.name);

  if (REPLACE) {
    const del = await Place.deleteMany({ name: { $in: names } });
    console.log(`🗑️  Removed ${del.deletedCount} existing places with matching names`);
  }

  let inserted = 0;
  let skipped = 0;

  for (const s of samples) {
    const exists = await Place.findOne({ name: s.name, country: s.country });
    if (exists && !REPLACE) {
      // ensure approved
      if (exists.approvalStatus !== "approved") {
        exists.approvalStatus = "approved";
        exists.adminFeedback = "Approved sample data";
        exists.reviewedBy = preet._id;
        exists.reviewedAt = new Date();
        if (!exists.addedBy) exists.addedBy = preet._id;
        await exists.save();
        console.log(`✓ Updated status → approved: ${s.name}`);
      } else {
        console.log(`· Skip (already exists): ${s.name}`);
      }
      skipped++;
      continue;
    }

    const place = new Place({
      name: s.name,
      shortDescription: s.shortDescription || "",
      description: s.description || s.shortDescription || "",
      city: s.city || "",
      state: s.state || "",
      country: s.country || "",
      location: s.location || "",
      imageUrl: s.imageUrl || "",
      addedBy: preet._id,
      averageRating: s.averageRating != null ? s.averageRating : 4.5,
      visitorsCount: s.visitorsCount != null ? s.visitorsCount : 0,
      ratings: [],
      approvalStatus: "approved",
      adminFeedback: "Official Travira sample place",
      reviewedBy: preet._id,
      reviewedAt: new Date()
    });

    await place.save();
    await User.findByIdAndUpdate(preet._id, { $addToSet: { addedPlaces: place._id } });
    inserted++;
    console.log(`+ Inserted: ${s.name} (${s.country})`);
  }

  console.log(`\nDone. Inserted: ${inserted}, skipped/updated: ${skipped}, total samples: ${samples.length}`);
  await mongoose.disconnect();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
