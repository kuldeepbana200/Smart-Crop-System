import { useEffect, useMemo, useState } from "react";
import { alertService, cropService, riskService } from "../services/api";

const riskTone = {
  LOW: "bg-emerald-100 text-emerald-800 border-emerald-200",
  MEDIUM: "bg-yellow-100 text-yellow-800 border-yellow-200",
  HIGH: "bg-orange-100 text-orange-800 border-orange-200",
  CRITICAL: "bg-red-100 text-red-800 border-red-200",
  default: "bg-slate-100 text-slate-700 border-slate-200",
};

const statusTone = {
  OPEN: "bg-amber-100 text-amber-800 border-amber-200",
  ACKNOWLEDGED: "bg-blue-100 text-blue-800 border-blue-200",
  RESOLVED: "bg-emerald-100 text-emerald-800 border-emerald-200",
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
    hour: "numeric",
    minute: "2-digit",
  }).format(date);
};

const RiskAlerts = () => {
  const [crops, setCrops] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [selectedCropId, setSelectedCropId] = useState("");
  const [assessment, setAssessment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [checking, setChecking] = useState(false);
  const [error, setError] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");

    try {
      const [cropsResponse, alertsResponse] = await Promise.all([
        cropService.getCrops(),
        alertService.getFarmerAlerts(),
      ]);

      const dataCrops = cropsResponse?.data || [];
      const dataAlerts = alertsResponse?.data || [];
      setCrops(dataCrops);
      setAlerts(dataAlerts);

      if (dataCrops.length > 0 && !selectedCropId) {
        setSelectedCropId(String(dataCrops[0].id));
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load risk and alert data right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAssessRisk = async () => {
    if (!selectedCropId) {
      setError("Select a crop to assess its risk.");
      return;
    }

    setChecking(true);
    setError("");

    try {
      const { data } = await riskService.assessRisk(Number(selectedCropId));
      setAssessment(data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to assess risk for this crop.",
      );
    } finally {
      setChecking(false);
    }
  };

  const visibleAlerts = useMemo(() => alerts || [], [alerts]);

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
                <div className="mt-4 h-8 w-28 rounded bg-slate-100" />
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
            Unable to load risk and alerts
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={loadData}
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
        <section className="rounded-3xl bg-gradient-to-r from-amber-500 to-orange-500 p-6 text-white shadow-lg shadow-orange-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-amber-100">
            Risk & alerts
          </p>
          <h1 className="mt-2 text-3xl font-bold">Field health overview</h1>
          <p className="mt-2 text-sm text-amber-50">
            Check crop risk and review the latest alerts affecting your farm.
          </p>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <h2 className="text-xl font-bold text-slate-900">
                Assess crop risk
              </h2>
              <p className="text-sm text-slate-500">
                Use the latest field and weather information.
              </p>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row">
              <select
                value={selectedCropId}
                onChange={(event) => setSelectedCropId(event.target.value)}
                disabled={crops.length === 0 || checking}
                className="rounded-xl border border-slate-300 bg-white px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100 disabled:cursor-not-allowed disabled:bg-slate-100"
              >
                {crops.length === 0 ? (
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
                onClick={handleAssessRisk}
                disabled={crops.length === 0 || checking}
                className="rounded-xl bg-amber-500 px-4 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {checking ? "Checking..." : "Check risk"}
              </button>
            </div>
          </div>
        </section>

        {assessment ? (
          <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
              <div>
                <p className="text-sm text-slate-500">Risk assessment</p>
                <h2 className="mt-1 text-2xl font-bold text-slate-900">
                  {assessment.cropName}
                </h2>
                <p className="text-sm text-slate-500">
                  {assessment.cropStage || "Stage not set"} •{" "}
                  {formatDate(assessment.assessedAt)}
                </p>
              </div>
              <span
                className={`inline-flex rounded-full border px-3 py-1 text-sm font-semibold ${riskTone[assessment.riskLevel] || riskTone.default}`}
              >
                {assessment.riskLevel || "UNKNOWN"} •{" "}
                {assessment.riskScore ?? 0}
              </span>
            </div>

            <div className="mt-5 grid gap-4 md:grid-cols-2">
              <div className="rounded-2xl border border-slate-200 p-4">
                <p className="text-sm font-medium text-slate-500">
                  Recommended action
                </p>
                <p className="mt-2 text-sm leading-6 text-slate-700">
                  {assessment.recommendedAction || "No action recommended yet."}
                </p>
              </div>
              <div className="rounded-2xl border border-slate-200 p-4">
                <p className="text-sm font-medium text-slate-500">
                  Risk factors
                </p>
                <div className="mt-3 space-y-2">
                  {(assessment.factors || []).length > 0 ? (
                    assessment.factors.map((factor, index) => (
                      <div
                        key={`${factor.type}-${index}`}
                        className="rounded-xl bg-slate-50 p-2 text-sm text-slate-700"
                      >
                        <div className="flex items-center justify-between gap-2">
                          <span className="font-medium">{factor.type}</span>
                          <span className="text-xs text-slate-500">
                            {factor.severity}
                          </span>
                        </div>
                        <p className="mt-1">{factor.reason}</p>
                      </div>
                    ))
                  ) : (
                    <p className="text-sm text-slate-500">
                      No specific risk factors reported.
                    </p>
                  )}
                </div>
              </div>
            </div>
          </section>
        ) : (
          crops.length > 0 && (
            <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
              <p className="text-xl font-semibold text-slate-900">
                No risk check yet
              </p>
              <p className="mt-2 text-sm text-slate-600">
                Choose a crop and run a risk assessment to view the latest field
                health summary.
              </p>
            </section>
          )
        )}

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-900">Alerts</h2>
            <span className="text-sm text-slate-500">
              {visibleAlerts.length} total
            </span>
          </div>

          {visibleAlerts.length === 0 ? (
            <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
              <p className="text-lg font-semibold text-slate-800">
                No alerts yet
              </p>
              <p className="mt-2 text-sm text-slate-600">
                Your field looks healthy right now. Check risk for a crop when
                you want a fresh review.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {visibleAlerts.map((alert) => (
                <article
                  key={alert.id}
                  className="rounded-2xl border border-slate-200 p-4"
                >
                  <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                    <div>
                      <p className="text-lg font-semibold text-slate-900">
                        {alert.cropName}
                      </p>
                      <p className="text-sm text-slate-500">
                        {formatDate(alert.createdAt)}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <span
                        className={`inline-flex rounded-full border px-2.5 py-1 text-[11px] font-semibold ${riskTone[alert.riskLevel] || riskTone.default}`}
                      >
                        {alert.riskLevel || "UNKNOWN"}
                      </span>
                      <span
                        className={`inline-flex rounded-full border px-2.5 py-1 text-[11px] font-semibold ${statusTone[alert.status] || statusTone.default}`}
                      >
                        {alert.status || "UNKNOWN"}
                      </span>
                    </div>
                  </div>

                  <div className="mt-4 grid gap-4 md:grid-cols-2">
                    <div>
                      <p className="text-sm font-medium text-slate-500">
                        Dominant factor
                      </p>
                      <p className="mt-1 text-sm text-slate-700">
                        {alert.dominantFactor || "Not specified"}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-500">
                        Recommended action
                      </p>
                      <p className="mt-1 text-sm text-slate-700">
                        {alert.recommendedAction || "Review field conditions."}
                      </p>
                    </div>
                  </div>

                  {(alert.factors || []).length > 0 && (
                    <div className="mt-4">
                      <p className="text-sm font-medium text-slate-500">
                        Factors
                      </p>
                      <div className="mt-2 flex flex-wrap gap-2">
                        {alert.factors.map((factor, index) => (
                          <span
                            key={`${alert.id}-${factor.type}-${index}`}
                            className="rounded-full bg-slate-100 px-2.5 py-1 text-xs text-slate-700"
                          >
                            {factor.type}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default RiskAlerts;
