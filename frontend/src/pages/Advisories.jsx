import { useEffect, useMemo, useState } from "react";
import { advisoryService, cropService } from "../services/api";

const severityStyles = {
  HIGH: "bg-red-100 text-red-700 border-red-200",
  MEDIUM: "bg-amber-100 text-amber-700 border-amber-200",
  LOW: "bg-emerald-100 text-emerald-700 border-emerald-200",
  default: "bg-slate-100 text-slate-700 border-slate-200",
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

const Advisories = () => {
  const [advisories, setAdvisories] = useState([]);
  const [crops, setCrops] = useState([]);
  const [selectedCropId, setSelectedCropId] = useState("");
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState("");

  const loadAdvisories = async () => {
    setLoading(true);
    setError("");

    try {
      const [advisoryResponse, cropResponse] = await Promise.all([
        advisoryService.getAdvisories(),
        cropService.getCrops(),
      ]);

      const loadedAdvisories = advisoryResponse?.data || [];
      const loadedCrops = cropResponse?.data || [];

      setAdvisories(loadedAdvisories);
      setCrops(loadedCrops);
      if (loadedCrops.length && !selectedCropId) {
        setSelectedCropId(String(loadedCrops[0].id));
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load advisory data right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdvisories();
  }, []);

  const hasCrops = crops.length > 0;

  const generateForSelectedCrop = async () => {
    if (!selectedCropId) {
      setError("Select a crop before generating advice.");
      return;
    }

    setGenerating(true);
    setError("");

    try {
      const { data } = await advisoryService.generateAdvisory(
        Number(selectedCropId),
      );
      setAdvisories((current) => [data, ...current]);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to generate advisory for this crop.",
      );
    } finally {
      setGenerating(false);
    }
  };

  const latestAdvice = useMemo(() => advisories[0] || null, [advisories]);

  if (loading) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-7xl space-y-4">
          <div className="animate-pulse rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
            <div className="h-6 w-40 rounded bg-slate-100" />
            <div className="mt-4 h-10 w-72 rounded bg-slate-100" />
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            {[0, 1].map((item) => (
              <div
                key={item}
                className="animate-pulse rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="h-4 w-24 rounded bg-slate-100" />
                <div className="mt-4 h-6 w-40 rounded bg-slate-100" />
                <div className="mt-4 h-16 w-full rounded bg-slate-100" />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-white p-6 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">
            Unable to load advisories
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={loadAdvisories}
            className="mt-5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-emerald-50 px-4 py-6">
      <div className="mx-auto max-w-7xl space-y-6">
        <section className="rounded-3xl bg-gradient-to-r from-emerald-600 to-green-500 p-6 text-white shadow-lg shadow-emerald-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-emerald-100">
            Advisories
          </p>
          <h1 className="mt-2 text-3xl font-bold">
            Smart guidance for your fields
          </h1>
          <p className="mt-2 text-sm text-emerald-50">
            Personalized crop advice based on your latest field and weather
            conditions.
          </p>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <h2 className="text-xl font-bold text-slate-900">
                Generate advice
              </h2>
              <p className="text-sm text-slate-500">
                Use the latest farm and weather data.
              </p>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row">
              <select
                value={selectedCropId}
                onChange={(event) => setSelectedCropId(event.target.value)}
                disabled={!hasCrops || generating}
                className="rounded-xl border border-slate-300 bg-white px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100 disabled:cursor-not-allowed disabled:bg-slate-100"
              >
                {!hasCrops ? (
                  <option value="">No crops available</option>
                ) : (
                  crops.map((crop) => (
                    <option key={crop.id} value={crop.id}>
                      {crop.cropName}
                    </option>
                  ))
                )}
              </select>

              <button
                type="button"
                onClick={generateForSelectedCrop}
                disabled={!hasCrops || generating}
                className="rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {generating ? "Generating..." : "Generate advisory"}
              </button>
            </div>
          </div>
        </section>

        {latestAdvice ? (
          <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Latest advisory</p>
            <div className="mt-3 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
              <div>
                <h2 className="text-2xl font-bold text-slate-900">
                  {latestAdvice.cropName || "Crop advisory"}
                </h2>
                <p className="text-sm text-slate-500">
                  {latestAdvice.cropStage || "Crop stage not set"} •{" "}
                  {formatDate(latestAdvice.generatedAt)}
                </p>
              </div>
              <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
                {latestAdvice.recommendations?.length || 0} recommendations
              </span>
            </div>
          </section>
        ) : null}

        {advisories.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-xl font-semibold text-slate-900">
              No advisory yet
            </p>
            <p className="mt-2 text-sm text-slate-600">
              {hasCrops
                ? "Generate an advisory for your crop to see practical field guidance here."
                : "Add a crop first, then generate advice for it."}
            </p>
          </section>
        ) : (
          <section className="space-y-4">
            {advisories.map((advisory) => (
              <article
                key={advisory.id}
                className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="text-lg font-bold text-slate-900">
                      {advisory.cropName}
                    </p>
                    <p className="text-sm text-slate-500">
                      {advisory.cropStage || "Crop stage not set"} •{" "}
                      {formatDate(advisory.generatedAt)}
                    </p>
                  </div>
                  <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
                    {advisory.recommendations?.length || 0} tips
                  </span>
                </div>

                <div className="mt-5 space-y-4">
                  {(advisory.recommendations || []).map((item, index) => (
                    <div
                      key={`${advisory.id}-${index}`}
                      className="rounded-2xl border border-slate-200 p-4"
                    >
                      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                          <p className="text-sm font-medium uppercase tracking-wide text-slate-500">
                            {item.category || "General"}
                          </p>
                          <h3 className="mt-1 text-lg font-semibold text-slate-900">
                            {item.title}
                          </h3>
                        </div>
                        <span
                          className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${severityStyles[item.severity?.toUpperCase()] || severityStyles.default}`}
                        >
                          {item.severity || "Info"}
                        </span>
                      </div>

                      <p className="mt-3 text-sm leading-6 text-slate-700">
                        {item.recommendation}
                      </p>
                      <p className="mt-3 text-sm italic text-slate-500">
                        Why: {item.reason}
                      </p>
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </section>
        )}
      </div>
    </div>
  );
};

export default Advisories;
