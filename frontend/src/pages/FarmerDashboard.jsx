import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import api from "../services/api";
import LanguageSelector from "../components/common/LanguageSelector";
import { getCropReference } from "../utils/cropReference";

const unavailable = "Not available";
const riskStyles = {
  LOW: "border-emerald-200 bg-emerald-50 text-emerald-700",
  MEDIUM: "border-amber-200 bg-amber-50 text-amber-700",
  HIGH: "border-orange-200 bg-orange-50 text-orange-700",
  CRITICAL: "border-red-200 bg-red-50 text-red-700",
};
const weatherLabels = {
  0: ["clearSky", "☀️"],
  1: ["mainlyClear", "🌤️"],
  2: ["partlyCloudy", "⛅"],
  3: ["overcast", "☁️"],
  45: ["fog", "🌫️"],
  51: ["drizzle", "🌦️"],
  61: ["rain", "🌧️"],
  71: ["snow", "❄️"],
  80: ["showers", "🌦️"],
  95: ["thunderstorm", "⛈️"],
};

const formatDate = (value) => {
  if (!value) return unavailable;
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric",
      }).format(date);
};
const formatTime = (value) => {
  if (!value) return unavailable;
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? unavailable
    : new Intl.DateTimeFormat("en-IN", {
        hour: "numeric",
        minute: "2-digit",
      }).format(date);
};
const stageDetails = (stage, cropName) =>
  getCropReference(cropName)?.stages?.find((item) => item.backend === stage) ||
  (stage
    ? { label: stage.replaceAll("_", " "), description: unavailable }
    : { label: unavailable, description: unavailable });

const localizedStage = (stage, t) => ({
  label: t(`cropStage.${stage.backend || "unknown"}.label`, {
    defaultValue: stage.label,
  }),
  description: t(`cropStage.${stage.backend || "unknown"}.description`, {
    defaultValue: stage.description,
  }),
});

const getGreetingKey = () => {
  const hour = new Date().getHours();
  if (hour < 12) return "dashboard.greeting.morning";
  if (hour < 17) return "dashboard.greeting.afternoon";
  return "dashboard.greeting.evening";
};

const Dashboard = () => {
  const { t } = useTranslation();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = async () => {
    setLoading(true);
    setError("");
    try {
      setDashboard((await api.get("/dashboard")).data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          t("dashboard.loadError", {
            defaultValue: "Unable to load dashboard data.",
          }),
      );
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    loadDashboard();
  }, []);

  const risk = useMemo(() => {
    const rank = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
    return (
      [...(dashboard?.recentAlerts || [])].sort(
        (a, b) => (rank[b.riskLevel] || 0) - (rank[a.riskLevel] || 0),
      )[0] || null
    );
  }, [dashboard]);

  if (loading)
    return (
      <div className="min-h-screen bg-[#f3f8f2] p-4 sm:p-8">
        <div className="mx-auto max-w-7xl animate-pulse space-y-5">
          <div className="h-28 rounded-3xl bg-white" />
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
            {[1, 2, 3, 4, 5].map((item) => (
              <div key={item} className="h-32 rounded-2xl bg-white" />
            ))}
          </div>
          <div className="h-80 rounded-3xl bg-white" />
        </div>
      </div>
    );
  if (error || !dashboard)
    return (
      <div className="min-h-screen bg-[#f3f8f2] p-6">
        <div className="mx-auto max-w-xl rounded-3xl bg-white p-8 text-center shadow-sm ring-1 ring-slate-200">
          <h1 className="text-xl font-bold">
            {t("dashboard.loadTitle", {
              defaultValue: "Dashboard unavailable",
            })}
          </h1>
          <p className="mt-2 text-sm text-slate-600">{error || unavailable}</p>
          <button
            type="button"
            onClick={loadDashboard}
            className="mt-5 rounded-xl bg-emerald-700 px-5 py-3 text-sm font-semibold text-white hover:bg-emerald-800"
          >
            {t("common.try_again", { defaultValue: "Try again" })}
          </button>
        </div>
      </div>
    );

  const {
    farmer,
    statistics,
    recentCrops = [],
    recentAlerts = [],
    recentAdvisories = [],
    recentNotifications = [],
    marketSummaries = [],
  } = dashboard;
  const unread = recentNotifications.filter((item) => item.status === "UNREAD");
  const currentCondition = weatherLabels[
    dashboard.weather?.current?.weatherCode
  ] || ["unknown", "🌤️"];
  const market = marketSummaries[0];
  const advisory = recentAdvisories[0];
  const riskCount = new Set(
    recentAlerts
      .filter((item) => ["HIGH", "CRITICAL"].includes(item.riskLevel))
      .map((item) => item.cropId),
  ).size;
  const greeting = t(getGreetingKey(), {
    defaultValue:
      getGreetingKey().split(".").pop() === "morning"
        ? "Good morning"
        : getGreetingKey().split(".").pop() === "afternoon"
          ? "Good afternoon"
          : "Good evening",
  });
  const weatherConditionLabel = t(`weather.${currentCondition[0]}`, {
    defaultValue: unavailable,
  });

  return (
    <main className="min-h-screen bg-[#f3f8f2] px-4 py-6 text-slate-900 sm:px-8">
      <div className="mx-auto max-w-7xl space-y-6">
        <header className="flex flex-col gap-4 rounded-3xl bg-white p-5 shadow-sm ring-1 ring-emerald-100 sm:flex-row sm:items-center sm:justify-between sm:p-7">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-emerald-700">
              {t("app.name", { defaultValue: "Smart Crop System" })}
            </p>
            <h1 className="mt-2 text-2xl font-bold tracking-tight sm:text-3xl">
              {greeting}, {farmer?.name || unavailable}!
            </h1>
            <p className="mt-1 text-slate-600">
              {t("dashboard.subtitle", {
                defaultValue: "Here's what's happening with your farm today.",
              })}
            </p>
            <p className="mt-3 text-sm font-medium text-slate-500">
              📍 {farmer?.district || unavailable},{" "}
              {farmer?.state || unavailable}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <LanguageSelector />
            <Link
              to="/notifications"
              aria-label={t("dashboard.notifications", {
                defaultValue: "Notifications",
              })}
              className="relative rounded-xl border border-slate-200 p-3 text-xl hover:border-emerald-400"
            >
              🔔
              {unread.length > 0 && (
                <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-red-600 px-1.5 text-center text-xs font-bold text-white">
                  {unread.length}
                </span>
              )}
            </Link>
          </div>
        </header>
        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
          <SummaryCard
            icon="☁️"
            label={t("dashboard.weather", { defaultValue: "Weather today" })}
          >
            <strong className="text-2xl">
              {dashboard.weather?.current?.temperature != null
                ? `${Math.round(dashboard.weather.current.temperature)}°C`
                : unavailable}
            </strong>
            <span>{weatherConditionLabel}</span>
            <small>
              {dashboard.weather?.current?.relativeHumidity != null
                ? `${dashboard.weather.current.relativeHumidity}% humidity`
                : unavailable}
            </small>
          </SummaryCard>
          <SummaryCard
            icon="🌧️"
            label={t("dashboard.rain", { defaultValue: "Rain forecast" })}
          >
            <strong className="text-2xl">
              {dashboard.weather?.forecastPrecipitation != null
                ? `${dashboard.weather.forecastPrecipitation} mm`
                : unavailable}
            </strong>
            <span>
              {dashboard.weather?.forecastPrecipitationProbability != null
                ? `${dashboard.weather.forecastPrecipitationProbability}% probability`
                : unavailable}
            </span>
            <small>
              {dashboard.weather?.forecastTimestamp
                ? formatDate(dashboard.weather.forecastTimestamp)
                : unavailable}
            </small>
          </SummaryCard>
          <SummaryCard
            icon="🛡️"
            label={t("dashboard.risk", { defaultValue: "Farm risk" })}
          >
            <strong
              className={`text-2xl ${risk?.riskLevel ? "" : "text-slate-400"}`}
            >
              {risk?.riskLevel || unavailable}
            </strong>
            <span>
              {riskCount
                ? t("common.affectedCrops", { count: riskCount })
                : t("common.unavailable", { defaultValue: unavailable })}
            </span>
            <small>{risk?.dominantFactor || unavailable}</small>
          </SummaryCard>
          <SummaryCard
            icon="₹"
            label={t("dashboard.market", { defaultValue: "Best market price" })}
          >
            <strong className="text-2xl">
              {market?.bestModalPrice != null
                ? `${market.currency || "₹"}${market.bestModalPrice}`
                : unavailable}
            </strong>
            <span>{market?.cropName || unavailable}</span>
            <small>{market?.bestMarket || unavailable}</small>
          </SummaryCard>
          <SummaryCard
            icon="🔔"
            label={t("dashboard.alerts", { defaultValue: "New alerts" })}
          >
            <strong className="text-2xl">
              {statistics?.unreadNotifications ?? unavailable}
            </strong>
            <span>
              {t("dashboard.unread", { defaultValue: "Unread notifications" })}
            </span>
            <small>
              {unread.length
                ? t("common.shownBelow", { count: unread.length })
                : t("common.unavailable", { defaultValue: unavailable })}
            </small>
          </SummaryCard>
        </section>
        <section className="grid gap-6 xl:grid-cols-[1.45fr_0.75fr]">
          <Panel
            title={t("dashboard.myCrops", { defaultValue: "My crops" })}
            action={
              <Link
                to="/crops"
                className="text-sm font-semibold text-emerald-700"
              >
                {t("common.view", { defaultValue: "View all" })}
              </Link>
            }
          >
            {recentCrops.length ? (
              <div className="grid gap-3 sm:grid-cols-2">
                {recentCrops.slice(0, 4).map((crop) => {
                  const stage = localizedStage(
                    stageDetails(crop.cropStage, crop.cropName),
                    t,
                  );
                  const cropRisk = recentAlerts.find(
                    (item) => item.cropId === crop.id,
                  );
                  return (
                    <article
                      key={crop.id}
                      className="rounded-2xl border border-slate-200 p-4 transition hover:border-emerald-300 hover:shadow-sm"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex gap-3">
                          <span className="grid h-11 w-11 place-items-center rounded-xl bg-emerald-100 text-2xl">
                            🌱
                          </span>
                          <div>
                            <h3 className="font-bold">
                              {crop.cropName || unavailable}
                            </h3>
                            <p className="text-sm text-emerald-700">
                              {stage.label}
                            </p>
                          </div>
                        </div>
                        {cropRisk && <RiskBadge level={cropRisk.riskLevel} />}
                      </div>
                      <p className="mt-3 text-sm text-slate-600">
                        {stage.description}
                      </p>
                      <p className="mt-2 text-xs text-slate-500">
                        {t("dashboard.planted", { defaultValue: "Planted" })}:{" "}
                        {formatDate(crop.sowingDate)}
                      </p>
                      <Link
                        to="/advisories"
                        className="mt-3 inline-block text-sm font-semibold text-emerald-700 hover:underline"
                      >
                        {t("dashboard.viewAdvisory", {
                          defaultValue: "View advisory",
                        })}{" "}
                        →
                      </Link>
                    </article>
                  );
                })}
              </div>
            ) : (
              <EmptyState
                text={t("dashboard.noCrops", {
                  defaultValue: "No crops added yet.",
                })}
              />
            )}
          </Panel>
          <aside className="rounded-3xl bg-gradient-to-br from-indigo-700 to-violet-700 p-6 text-white shadow-sm">
            <p className="text-3xl">🌾</p>
            <h2 className="mt-4 text-2xl font-bold">
              {t("dashboard.advisorTitle", { defaultValue: "My Farm Advisor" })}
            </h2>
            <p className="mt-2 text-sm leading-6 text-indigo-100">
              {t("dashboard.advisorSubtitle", {
                defaultValue:
                  "Personal recommendations based on your crops, weather and farm data.",
              })}
            </p>
            <Link
              to="/advisories"
              className="mt-6 inline-flex rounded-xl bg-white px-4 py-3 text-sm font-bold text-indigo-700 hover:bg-indigo-50"
            >
              {t("dashboard.getAdvisory", { defaultValue: "Get my advisory" })}{" "}
              →
            </Link>
          </aside>
        </section>
        <section className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <Panel
            title={t("dashboard.topAdvisory", {
              defaultValue: "Today's top advisory",
            })}
            action={
              <Link
                to="/advisories"
                className="text-sm font-semibold text-emerald-700"
              >
                {t("common.view", { defaultValue: "View all" })}
              </Link>
            }
          >
            {advisory ? (
              <div className="rounded-2xl border border-amber-200 bg-amber-50 p-5">
                <span className="text-xs font-bold uppercase tracking-wider text-amber-700">
                  {t("dashboard.available", {
                    defaultValue: "Priority advisory",
                  })}
                </span>
                <h3 className="mt-2 text-lg font-bold">
                  {advisory.cropName || unavailable}
                </h3>
                <p className="mt-2 text-sm text-slate-700">
                  {advisory.recommendationCount}{" "}
                  {t("dashboard.recommendations", {
                    defaultValue: "recommendations available",
                  })}
                  .{" "}
                  {t("dashboard.readFull", {
                    defaultValue: "Open Advisories to read the full guidance.",
                  })}
                </p>
                <div className="mt-4 flex flex-wrap gap-4 text-xs text-slate-600">
                  <span>
                    {t("dashboard.stage", { defaultValue: "Stage" })}:{" "}
                    {
                      localizedStage(
                        stageDetails(advisory.cropStage, advisory.cropName),
                        t,
                      ).label
                    }
                  </span>
                  <span>{formatTime(advisory.generatedAt)}</span>
                </div>
                <Link
                  to="/advisories"
                  className="mt-4 inline-block text-sm font-bold text-amber-800 hover:underline"
                >
                  {t("dashboard.readAdvisory", {
                    defaultValue: "Read full advisory",
                  })}{" "}
                  →
                </Link>
              </div>
            ) : (
              <EmptyState
                text={t("dashboard.noAdvisory", {
                  defaultValue: "No new advisory for your crops.",
                })}
              />
            )}
          </Panel>
          <Panel
            title={t("dashboard.marketSummary", {
              defaultValue: "Market price summary",
            })}
            action={
              <Link
                to="/market"
                className="text-sm font-semibold text-emerald-700"
              >
                {t("common.view", { defaultValue: "View prices" })}
              </Link>
            }
          >
            {market ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-bold">{market.cropName}</h3>
                  <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700">
                    {t("common.markets", { count: market.marketsTracked })}
                  </span>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <Metric
                    label={t("dashboard.best", {
                      defaultValue: "Best available",
                    })}
                    value={
                      market.bestModalPrice != null
                        ? `${market.currency || "₹"}${market.bestModalPrice}`
                        : unavailable
                    }
                  />
                  <Metric
                    label={t("dashboard.marketLocation", {
                      defaultValue: "Market",
                    })}
                    value={market.bestMarket || unavailable}
                  />
                </div>
                <p className="text-xs text-slate-500">
                  {t("dashboard.observed", {
                    defaultValue: "Latest observation",
                  })}
                  : {formatDate(market.latestObservation)}
                </p>
              </div>
            ) : (
              <EmptyState
                text={t("dashboard.noMarket", {
                  defaultValue: "Market price unavailable for your crops.",
                })}
              />
            )}
          </Panel>
        </section>
        <section className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
          <Panel
            title={t("dashboard.recentAlerts", {
              defaultValue: "Recent alerts",
            })}
            action={
              <Link
                to="/notifications"
                className="text-sm font-semibold text-emerald-700"
              >
                {t("common.view", { defaultValue: "View all" })}
              </Link>
            }
          >
            {recentNotifications.length ? (
              <div className="space-y-3">
                {recentNotifications.slice(0, 4).map((notification) => (
                  <div
                    key={notification.id}
                    className={`rounded-2xl border p-4 ${notification.status === "UNREAD" ? "border-amber-200 bg-amber-50/60" : "border-slate-200"}`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="font-semibold">
                          {notification.title ||
                            notification.type ||
                            unavailable}
                        </p>
                        <p className="mt-1 text-sm text-slate-600">
                          {notification.message || unavailable}
                        </p>
                      </div>
                      {notification.status === "UNREAD" && (
                        <span
                          className="h-2.5 w-2.5 rounded-full bg-emerald-600"
                          aria-label={t("dashboard.unread", {
                            defaultValue: "Unread",
                          })}
                        />
                      )}
                    </div>
                    <p className="mt-2 text-xs text-slate-500">
                      {formatTime(notification.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <EmptyState
                text={t("dashboard.noNotifications", {
                  defaultValue: "No recent notifications.",
                })}
              />
            )}
          </Panel>
          <Panel
            title={t("dashboard.weatherDetails", {
              defaultValue: "Weather at your farm",
            })}
            action={
              <Link
                to="/weather"
                className="text-sm font-semibold text-emerald-700"
              >
                {t("common.view", { defaultValue: "Full forecast" })}
              </Link>
            }
          >
            {
              <div className="rounded-2xl bg-sky-50 p-5">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-4xl">{currentCondition[1]}</p>
                    <p className="mt-2 text-2xl font-bold">
                      {dashboard.weather?.current?.temperature != null
                        ? `${Math.round(dashboard.weather.current.temperature)}°C`
                        : unavailable}
                    </p>
                    <p className="text-sm text-slate-600">
                      {weatherConditionLabel}
                    </p>
                  </div>
                  <div className="text-right text-sm text-slate-600">
                    <p>
                      {t("dashboard.humidity", { defaultValue: "Humidity" })}
                    </p>
                    <p className="font-bold text-slate-900">
                      {dashboard.weather?.current?.relativeHumidity != null
                        ? `${dashboard.weather.current.relativeHumidity}%`
                        : unavailable}
                    </p>
                    <p className="mt-3">
                      {t("dashboard.wind", { defaultValue: "Wind" })}
                    </p>
                    <p className="font-bold text-slate-900">
                      {dashboard.weather?.current?.windSpeed != null
                        ? `${dashboard.weather.current.windSpeed} km/h`
                        : unavailable}
                    </p>
                  </div>
                </div>
              </div>
            }
          </Panel>
        </section>
      </div>
    </main>
  );
};

const SummaryCard = ({ icon, label, children }) => (
  <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
    <div className="flex items-center gap-2 text-sm font-semibold text-slate-500">
      <span className="text-lg">{icon}</span>
      {label}
    </div>
    <div className="mt-3 flex flex-col gap-1">{children}</div>
  </div>
);
const Panel = ({ title, action, children }) => (
  <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-200 sm:p-6">
    <div className="mb-5 flex items-center justify-between gap-3">
      <h2 className="text-xl font-bold">{title}</h2>
      {action}
    </div>
    {children}
  </section>
);
const EmptyState = ({ text }) => (
  <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-6 text-center text-sm text-slate-600">
    {text}
  </div>
);
const RiskBadge = ({ level }) => (
  <span
    className={`rounded-full border px-2.5 py-1 text-xs font-bold ${riskStyles[level] || "border-slate-200 bg-slate-50 text-slate-600"}`}
  >
    {level || unavailable}
  </span>
);
const Metric = ({ label, value }) => (
  <div className="rounded-xl bg-slate-50 p-3">
    <p className="text-xs text-slate-500">{label}</p>
    <p className="mt-1 break-words font-semibold">{value}</p>
  </div>
);

export default Dashboard;
