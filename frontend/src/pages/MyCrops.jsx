import { useEffect, useMemo, useState } from "react";
import { cropService } from "../services/api";
import {
  cropNameSuggestions,
  estimateHarvestDate,
  getCropReference,
  getPlantingGuidance,
  getStageOptions,
} from "../utils/cropReference";

const emptyForm = {
  cropName: "",
  cropStage: "",
  sowingDate: "",
  expectedHarvestDate: "",
};

const formatDate = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
};

const MyCrops = () => {
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [cropSearch, setCropSearch] = useState("");
  const [isCropSuggestionsOpen, setIsCropSuggestionsOpen] = useState(false);
  const [farmerProfile, setFarmerProfile] = useState({
    state: "",
    district: "",
  });

  const selectedCropReference = useMemo(
    () => getCropReference(form.cropName),
    [form.cropName],
  );
  const stageOptions = useMemo(
    () => getStageOptions(form.cropName),
    [form.cropName],
  );
  const plantingGuidance = useMemo(
    () =>
      getPlantingGuidance(
        form.cropName,
        farmerProfile.state,
        farmerProfile.district,
      ),
    [form.cropName, farmerProfile.state, farmerProfile.district],
  );
  const cropSuggestions = useMemo(() => {
    const term = cropSearch.trim().toLowerCase();
    if (!term) {
      return cropNameSuggestions.slice(0, 12);
    }
    return cropNameSuggestions.filter((name) =>
      name.toLowerCase().includes(term),
    );
  }, [cropSearch]);

  const loadCrops = async () => {
    setLoading(true);
    setError("");

    try {
      const { data } = await cropService.getCrops();
      setCrops(data || []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load your crops right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCrops();
  }, []);

  const summary = useMemo(() => {
    if (!crops.length) {
      return { total: 0, active: 0, recent: "No crops yet" };
    }

    const active = crops.filter((crop) => crop.cropStage).length;
    const recent = crops[0]?.cropName || "Latest crop";

    return {
      total: crops.length,
      active,
      recent,
    };
  }, [crops]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => {
      const updated = { ...current, [name]: value };

      if (name === "cropName") {
        const cropRef = getCropReference(value);
        setCropSearch(value);
        if (
          cropRef &&
          !cropRef.stages.some((stage) => stage.backend === updated.cropStage)
        ) {
          updated.cropStage = cropRef.stages[0]?.backend || "";
        }
      }

      if (name === "sowingDate" && value && updated.cropName) {
        updated.expectedHarvestDate = estimateHarvestDate(
          updated.cropName,
          value,
        );
      }

      return updated;
    });
  };

  const handleCropSelect = (cropName) => {
    setForm((current) => {
      const cropRef = getCropReference(cropName);
      return {
        ...current,
        cropName,
        cropStage: cropRef?.stages?.[0]?.backend || current.cropStage || "",
      };
    });
    setCropSearch(cropName);
    setIsCropSuggestionsOpen(false);
    if (form.sowingDate) {
      setForm((current) => ({
        ...current,
        expectedHarvestDate: estimateHarvestDate(cropName, current.sowingDate),
      }));
    }
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    if (
      !form.cropName ||
      !form.cropStage ||
      !form.sowingDate ||
      !form.expectedHarvestDate
    ) {
      setError("Please complete all crop details before saving.");
      setSaving(false);
      return;
    }

    if (new Date(form.expectedHarvestDate) < new Date(form.sowingDate)) {
      setError("Harvest date should be after the planting date.");
      setSaving(false);
      return;
    }

    try {
      if (editingId) {
        await cropService.updateCrop(editingId, form);
      } else {
        await cropService.createCrop(form);
      }
      resetForm();
      await loadCrops();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to save crop details. Please try again.",
      );
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (crop) => {
    setEditingId(crop.id);
    setForm({
      cropName: crop.cropName || "",
      cropStage: crop.cropStage || "",
      sowingDate: crop.sowingDate || "",
      expectedHarvestDate: crop.expectedHarvestDate || "",
    });
    setCropSearch(crop.cropName || "");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this crop?")) {
      return;
    }

    try {
      await cropService.deleteCrop(id);
      if (editingId === id) {
        resetForm();
      }
      await loadCrops();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to delete the crop right now.",
      );
    }
  };

  useEffect(() => {
    const fetchFarmerProfile = async () => {
      try {
        const { data } = await fetch("/api/farmers/profile").then((res) =>
          res.json(),
        );
        setFarmerProfile({
          state: data?.state || "",
          district: data?.district || "",
        });
      } catch (error) {
        setFarmerProfile({ state: "", district: "" });
      }
    };

    fetchFarmerProfile();
  }, []);

  const selectedStage =
    stageOptions.find((stage) => stage.backend === form.cropStage) || null;
  const plantingWarning =
    form.cropName &&
    form.sowingDate &&
    selectedCropReference &&
    selectedCropReference.plantingPeriod &&
    selectedCropReference.plantingPeriod !== "General planting guidance" &&
    !selectedCropReference.plantingPeriod.toLowerCase().includes("varies") &&
    !selectedCropReference.plantingPeriod.toLowerCase().includes("guidance") &&
    (() => {
      const month = new Date(`${form.sowingDate}T00:00:00`).getMonth();
      const plantingMonths =
        selectedCropReference.plantingPeriod
          .match(/\d{4}|[A-Za-z]+/g)
          ?.filter((part) => !/^\d+$/.test(part)) || [];

      const monthMap = {
        January: 0,
        February: 1,
        March: 2,
        April: 3,
        May: 4,
        June: 5,
        July: 6,
        August: 7,
        September: 8,
        October: 9,
        November: 10,
        December: 11,
      };

      if (plantingMonths.length === 0) return "";
      const allowedMonths = plantingMonths
        .map((monthName) => monthMap[monthName])
        .filter(Boolean);
      if (allowedMonths.length === 0) return "";
      return allowedMonths.includes(month)
        ? ""
        : `⚠️ This is outside the usual planting period for ${form.cropName}.`;
    })();

  return (
    <div className="min-h-screen bg-emerald-50 px-4 py-6">
      <div className="mx-auto max-w-7xl space-y-6">
        <section className="rounded-3xl bg-gradient-to-r from-emerald-600 to-green-500 p-6 text-white shadow-lg shadow-emerald-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-emerald-100">
            My Crops
          </p>
          <h1 className="mt-2 text-3xl font-bold">Your field records</h1>
          <p className="mt-2 text-sm text-emerald-50">
            Track crops, planting dates, and harvest plans.
          </p>
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Total crops</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.total}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Active stages</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.active}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Latest crop</p>
            <p className="mt-3 text-lg font-semibold text-slate-900">
              {summary.recent}
            </p>
          </div>
        </section>

        <section className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
          <form
            onSubmit={handleSubmit}
            className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
          >
            <div className="mb-5 flex items-center justify-between gap-3">
              <h2 className="text-xl font-bold text-slate-900">
                {editingId ? "Update crop" : "Add a crop"}
              </h2>
              {editingId && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="text-sm font-medium text-slate-600 hover:text-slate-800"
                >
                  Cancel
                </button>
              )}
            </div>

            <div className="space-y-4">
              <div className="relative">
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  Crop name
                </label>
                <input
                  name="cropName"
                  value={cropSearch}
                  onChange={(event) => {
                    setCropSearch(event.target.value);
                    setIsCropSuggestionsOpen(true);
                    setForm((current) => ({
                      ...current,
                      cropName: event.target.value,
                    }));
                  }}
                  onFocus={() => setIsCropSuggestionsOpen(true)}
                  required
                  className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                  placeholder="Search crop..."
                />
                {isCropSuggestionsOpen && cropSuggestions.length > 0 && (
                  <div className="absolute z-10 mt-1 max-h-52 w-full overflow-y-auto rounded-xl border border-slate-200 bg-white p-1 shadow-lg">
                    {cropSuggestions.map((name) => (
                      <button
                        key={name}
                        type="button"
                        onClick={() => handleCropSelect(name)}
                        className="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-sm text-slate-700 hover:bg-emerald-50"
                      >
                        <span>{name}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {selectedCropReference && (
                <div className="rounded-xl border border-emerald-100 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
                  <div className="font-medium">Usual planting period</div>
                  <div>{plantingGuidance.detail}</div>
                </div>
              )}

              <div>
                <label className="mb-2 block text-sm font-medium text-slate-700">
                  Crop stage
                </label>
                <select
                  name="cropStage"
                  value={form.cropStage}
                  onChange={handleChange}
                  className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                >
                  {!form.cropName && (
                    <option value="">Select a crop first</option>
                  )}
                  {stageOptions.length === 0 && form.cropName && (
                    <option value="">No stages available</option>
                  )}
                  {stageOptions.map((stage) => (
                    <option key={stage.backend} value={stage.backend}>
                      {stage.label}
                    </option>
                  ))}
                </select>
                {selectedStage && (
                  <p className="mt-2 text-xs text-slate-600">
                    {selectedStage.label} — {selectedStage.description}
                  </p>
                )}
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">
                    Planting date
                  </label>
                  <input
                    type="date"
                    name="sowingDate"
                    value={form.sowingDate}
                    onChange={handleChange}
                    required
                    className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                  />
                </div>

                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">
                    Expected harvest date
                  </label>
                  <input
                    type="date"
                    name="expectedHarvestDate"
                    value={form.expectedHarvestDate}
                    onChange={handleChange}
                    required
                    className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                  />
                </div>
              </div>

              {plantingWarning && (
                <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
                  {plantingWarning}
                  <div className="mt-1 text-xs text-amber-700">
                    Usual period: {selectedCropReference.plantingPeriod}
                  </div>
                </div>
              )}

              {form.cropName && form.sowingDate && (
                <div className="rounded-xl border border-sky-200 bg-sky-50 px-3 py-2 text-sm text-sky-800">
                  Estimated harvest date:{" "}
                  {form.expectedHarvestDate
                    ? formatDate(form.expectedHarvestDate)
                    : "—"}
                </div>
              )}

              {error && (
                <div className="rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={saving}
                className="w-full rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {saving
                  ? editingId
                    ? "Updating crop..."
                    : "Saving crop..."
                  : editingId
                    ? "Update crop"
                    : "Save crop"}
              </button>
            </div>
          </form>

          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-5 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">Crop list</h2>
              <span className="text-sm text-slate-500">
                {crops.length} records
              </span>
            </div>

            {loading ? (
              <div className="space-y-3">
                {[0, 1, 2].map((item) => (
                  <div
                    key={item}
                    className="animate-pulse rounded-2xl border border-slate-200 p-4"
                  >
                    <div className="h-5 w-28 rounded bg-slate-100" />
                    <div className="mt-3 h-4 w-40 rounded bg-slate-100" />
                    <div className="mt-4 h-4 w-48 rounded bg-slate-100" />
                  </div>
                ))}
              </div>
            ) : crops.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-lg font-semibold text-slate-800">
                  No crops yet
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Add your first crop to begin tracking your farm.
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {crops.map((crop) => (
                  <div
                    key={crop.id}
                    className="rounded-2xl border border-slate-200 p-4"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-lg font-semibold text-slate-900">
                          {crop.cropName}
                        </p>
                        <p className="text-sm text-slate-500">
                          {crop.cropStage || "Stage not set"}
                        </p>
                      </div>
                      <span className="rounded-full bg-emerald-100 px-2.5 py-1 text-[11px] font-medium text-emerald-700">
                        Crop
                      </span>
                    </div>

                    <div className="mt-3 grid gap-2 text-sm text-slate-600 sm:grid-cols-2">
                      <p>Planting: {formatDate(crop.sowingDate)}</p>
                      <p>Harvest: {formatDate(crop.expectedHarvestDate)}</p>
                    </div>

                    <div className="mt-4 flex gap-2">
                      <button
                        type="button"
                        onClick={() => handleEdit(crop)}
                        className="rounded-lg bg-slate-100 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-200"
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(crop.id)}
                        className="rounded-lg bg-red-50 px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-100"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
};

export default MyCrops;
