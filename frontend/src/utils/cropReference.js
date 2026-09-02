export const cropReferenceOptions = [
  {
    name: "Rice",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "TILLERING", label: "Tillering", description: "More shoots are growing" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "GRAIN_FILLING", label: "Grain Filling", description: "Grains are developing" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "June–July",
    durationDays: 120,
  },
  {
    name: "Wheat",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "GRAIN_FILLING", label: "Grain Filling", description: "Grains are developing" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "November–December",
    durationDays: 150,
  },
  {
    name: "Cotton",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "BOLL_DEVELOPMENT", label: "Boll Development", description: "Bolls are growing" },
      { backend: "BOLL_OPENING", label: "Boll Opening", description: "Bolls are opening" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "April–June",
    durationDays: 180,
  },
  {
    name: "Sugarcane",
    stages: [
      { backend: "PLANTING", label: "Planting", description: "Crop has been planted" },
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "TILLERING", label: "Tillering", description: "More shoots are growing" },
      { backend: "GRAND_GROWTH", label: "Grand Growth", description: "Plant is growing quickly" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "Planting period varies by region",
    durationDays: 300,
  },
  {
    name: "Maize",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "GRAIN_FILLING", label: "Grain Filling", description: "Grains are developing" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "June–July",
    durationDays: 120,
  },
  {
    name: "Groundnut",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "POD_DEVELOPMENT", label: "Pod Development", description: "Pods are forming under the soil" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "June–July",
    durationDays: 110,
  },
  {
    name: "Mustard",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "GRAIN_FILLING", label: "Pod / Grain Filling", description: "Seeds are developing" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "October–November",
    durationDays: 120,
  },
  {
    name: "Soybean",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "POD_DEVELOPMENT", label: "Pod Development", description: "Pods are forming" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "June–July",
    durationDays: 110,
  },
  {
    name: "Chickpea",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "POD_DEVELOPMENT", label: "Pod Development", description: "Pods are forming" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "October–November",
    durationDays: 130,
  },
  {
    name: "Tomato",
    stages: [
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "FRUITING", label: "Fruiting", description: "Fruits are developing" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "October–December",
    durationDays: 100,
  },
  {
    name: "Potato",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "TUBER_DEVELOPMENT", label: "Tuber Development", description: "Tubers are growing under the soil" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "October–December",
    durationDays: 120,
  },
  {
    name: "Onion",
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "BULB_DEVELOPMENT", label: "Bulb Development", description: "Bulb is forming" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "October–December",
    durationDays: 110,
  },
];

export const cropNameSuggestions = cropReferenceOptions.map((crop) => crop.name);

export const getCropReference = (cropName) => {
  const normalized = (cropName || "").trim();
  if (!normalized) {
    return null;
  }

  return cropReferenceOptions.find(
    (crop) => crop.name.toLowerCase() === normalized.toLowerCase(),
  ) || {
    name: normalized,
    stages: [
      { backend: "GERMINATION", label: "Germination", description: "Seed has started growing" },
      { backend: "SEEDLING", label: "Seedling", description: "Young plant with small leaves" },
      { backend: "VEGETATIVE", label: "Vegetative Growth", description: "Plant is growing leaves and stems" },
      { backend: "FLOWERING", label: "Flowering", description: "Plant is producing flowers" },
      { backend: "MATURITY", label: "Maturity", description: "Crop is almost ready to harvest" },
      { backend: "HARVESTING", label: "Harvesting", description: "Crop is ready to harvest" },
    ],
    plantingPeriod: "General planting guidance",
    durationDays: 120,
  };
};

export const getStageOptions = (cropName) => getCropReference(cropName)?.stages || [];

export const getPlantingGuidance = (cropName, state, district) => {
  const crop = getCropReference(cropName);
  if (!crop) {
    return {
      title: "General planting guidance",
      detail: "Planting guidance unavailable",
    };
  }

  const locationText = state || district ? ` for ${state || district}` : "";

  return {
    title: crop.plantingPeriod === "General planting guidance" ? "General planting guidance" : `Usual planting period${locationText}`,
    detail:
      crop.plantingPeriod === "General planting guidance"
        ? "Planting guidance unavailable"
        : `${crop.plantingPeriod}${locationText ? ` (${state || district})` : ""}`,
  };
};

export const estimateHarvestDate = (cropName, sowingDate) => {
  if (!sowingDate || !cropName) return "";

  const parsed = new Date(`${sowingDate}T00:00:00`);
  if (Number.isNaN(parsed.getTime())) return "";

  const crop = getCropReference(cropName);
  const days = crop?.durationDays || 120;
  const estimated = new Date(parsed);
  estimated.setDate(estimated.getDate() + days);

  return estimated.toISOString().slice(0, 10);
};
