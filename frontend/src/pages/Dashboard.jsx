import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import api, { weatherService } from "../services/api";

const riskTone = {
  LOW: "bg-emerald-100 text-emerald-800 border-emerald-200",
  MEDIUM: "bg-yellow-100 text-yellow-800 border-yellow-200",
  HIGH: "bg-orange-100 text-orange-800 border-orange-200",
  CRITICAL: "bg-red-100 text-red-800 border-red-200",
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

const Dashboard = () => {
  const [dashboard, setDashboard] = useState(null);
  const [currentWeather, setCurrentWeather] = useState(null);
  const [todayForecast, setTodayForecast] = useState([]);
  const [loading, setLoading] = useState(true);
  const [weatherLoading, setWeatherLoading] = useState(true);
  const [error, setError] = useState(null);
  const [weatherError, setWeatherError] = useState("");

  const loadDashboard = async () => {
    setLoading(true);
    setError(null);

    try {
      const { data } = await api.get("/dashboard");
      setDashboard(data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load dashboard data.",
      );
    } finally {
      setLoading(false);
    }
  };

  const loadWeather = async () => {
    setWeatherLoading(true);
    setWeatherError("");

    try {
      const [currentResponse, forecastResponse] = await Promise.all([
        weatherService.getCurrentWeather(),
        weatherService.getForecast(),
      ]);

      const current = currentResponse?.data || null;
      const forecast = forecastResponse?.data || null;
      setCurrentWeather(current);

      const hourlyTimes = forecast?.hourly?.time || [];
      const hourlyTemps = forecast?.hourly?.temperature || [];
      const hourlyWeatherCodes = forecast?.hourly?.weatherCode || [];

      const upcoming = hourlyTimes
        .map((time, index) => ({
          time,
          temperature: hourlyTemps[index],
          weatherCode: hourlyWeatherCodes[index],
        }))
        .slice(0, 6);

      setTodayForecast(upcoming);
    } catch (err) {
      setWeatherError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load weather for your farm.",
      );
    } finally {
      setWeatherLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
    loadWeather();
  }, []);

  const weatherHint = useMemo(() => {
    if (!currentWeather) return "Add your farm location to see local weather.";
    return `${Math.round(currentWeather.temperature)}°C now • ${currentWeather.relativeHumidity ?? "humidity"}% humidity`;
  }, [currentWeather]);

  if (loading) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-7xl space-y-4">
          <div className="animate-pulse rounded-2xl bg-white p-6 shadow-sm ring-1 ring-emerald-100">
            <div className="h-6 w-40 rounded bg-emerald-100" />
            <div className="mt-4 h-4 w-72 rounded bg-slate-100" />
          </div>
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {[0, 1, 2, 3].map((item) => (
              <div
                key={item}
                className="animate-pulse rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="h-4 w-20 rounded bg-slate-100" />
                <div className="mt-4 h-8 w-16 rounded bg-slate-100" />
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
            Unable to load dashboard
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={loadDashboard}
            className="mt-5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  if (!dashboard) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl rounded-2xl border border-slate-200 bg-white p-6 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">
            No dashboard data available yet
          </p>
          <p className="mt-2 text-sm text-slate-600">
            Add a crop or wait for the latest farm updates to appear here.
          </p>
        </div>
      </div>
    );
  }

  const {
    farmer,
    statistics,
    recentCrops = [],
    recentAlerts = [],
    recentAdvisories = [],
    recentNotifications = [],
    marketSummaries = [],
  } = dashboard;
  const latestAlert = recentAlerts[0];
  const latestAdvisory = recentAdvisories[0];
  const unreadNotifications = recentNotifications.filter(
    (notification) => notification.status === "UNREAD",
  );

  const weatherCodeMap = {
    0: { label: "Clear sky", icon: "☀️" },
    1: { label: "Mainly clear", icon: "🌤️" },
    2: { label: "Partly cloudy", icon: "⛅" },
    3: { label: "Overcast", icon: "☁️" },
    45: { label: "Fog", icon: "🌫️" },
    51: { label: "Drizzle", icon: "🌦️" },
    61: { label: "Rain", icon: "🌧️" },
    71: { label: "Snow", icon: "❄️" },
    80: { label: "Showers", icon: "🌦️" },
    95: { label: "Thunderstorm", icon: "⛈️" },
  };

  const weatherCondition = currentWeather?.weatherCode
    ? weatherCodeMap[currentWeather.weatherCode] || {
        label: "Weather update",
        icon: "🌤️",
      }
    : { label: "Weather update", icon: "🌤️" };

  return (
    <div className="min-h-screen bg-emerald-50 px-4 py-6">
      <div className="mx-auto max-w-7xl space-y-6">
        <section className="rounded-3xl bg-gradient-to-r from-emerald-600 to-green-500 p-6 text-white shadow-lg shadow-emerald-100">
          <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm font-medium uppercase tracking-[0.2em] text-emerald-100">
                Smart Crop
              </p>
              <h1 className="mt-2 text-3xl font-bold leading-tight">
                Good morning, {farmer?.name || "Farmer"}
              </h1>
              <p className="mt-2 text-sm text-emerald-50">
                {farmer?.district || "Your district"},{" "}
                {farmer?.state || "Your state"}
              </p>
            </div>

            <div className="rounded-2xl bg-white/10 p-4 backdrop-blur-sm ring-1 ring-white/20">
              <p className="text-xs uppercase tracking-[0.2em] text-emerald-100">
                Today
              </p>
              <p className="mt-2 text-xl font-semibold">{weatherHint}</p>
            </div>
          </div>

          <div className="mt-5 grid gap-3 sm:grid-cols-3">
            <Link
              to="/weather"
              className="rounded-2xl bg-white/10 p-4 text-left ring-1 ring-white/20 transition hover:bg-white/15"
            >
              <p className="text-sm text-emerald-100">Weather</p>
              <p className="mt-2 text-lg font-semibold">Check today</p>
            </Link>
            <Link
              to="/crops"
              className="rounded-2xl bg-white/10 p-4 text-left ring-1 ring-white/20 transition hover:bg-white/15"
            >
              <p className="text-sm text-emerald-100">My Crops</p>
              <p className="mt-2 text-lg font-semibold">Manage fields</p>
            </Link>
            <Link
              to="/market"
              className="rounded-2xl bg-white/10 p-4 text-left ring-1 ring-white/20 transition hover:bg-white/15"
            >
              <p className="text-sm text-emerald-100">Market</p>
              <p className="mt-2 text-lg font-semibold">See prices</p>
            </Link>
          </div>
        </section>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Crops</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {statistics?.totalCrops ?? 0}
            </p>
            <p className="mt-2 text-sm text-emerald-600">Active fields</p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Open alerts</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {statistics?.openDistressAlerts ?? 0}
            </p>
            <p className="mt-2 text-sm text-amber-600">Needs attention</p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Advisories</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {statistics?.totalAdvisories ?? 0}
            </p>
            <p className="mt-2 text-sm text-blue-600">Actionable tips</p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Unread</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {statistics?.unreadNotifications ?? 0}
            </p>
            <p className="mt-2 text-sm text-violet-600">Messages</p>
          </div>
        </section>

        <section className="grid gap-6 xl:grid-cols-[1.4fr_1fr]">
          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">Weather</h2>
              <Link
                to="/weather"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                Full forecast
              </Link>
            </div>

            {weatherLoading ? (
              <div className="animate-pulse rounded-2xl bg-slate-50 p-4">
                <div className="h-5 w-32 rounded bg-slate-200" />
                <div className="mt-4 h-10 w-28 rounded bg-slate-200" />
              </div>
            ) : weatherError ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  Location needed for weather
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Save your field location to fetch live weather for your farm.
                </p>
                <Link
                  to="/profile"
                  className="mt-4 inline-flex rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
                >
                  Update location
                </Link>
              </div>
            ) : currentWeather ? (
              <div className="space-y-4">
                <div className="flex items-start justify-between gap-4 rounded-2xl bg-gradient-to-r from-sky-50 to-emerald-50 p-4 ring-1 ring-sky-100">
                  <div>
                    <p className="text-sm text-slate-500">Current weather</p>
                    <div className="mt-2 flex items-center gap-3">
                      <span className="text-3xl">{weatherCondition.icon}</span>
                      <div>
                        <p className="text-3xl font-bold text-slate-900">
                          {Math.round(currentWeather.temperature)}°C
                        </p>
                        <p className="text-sm text-slate-600">
                          {weatherCondition.label}
                        </p>
                      </div>
                    </div>
                  </div>
                  <div className="text-right text-sm text-slate-600">
                    <p>Humidity</p>
                    <p className="font-semibold text-slate-900">
                      {currentWeather.relativeHumidity ?? "—"}%
                    </p>
                    <p className="mt-2">Wind</p>
                    <p className="font-semibold text-slate-900">
                      {currentWeather.windSpeed ?? "—"} km/h
                    </p>
                  </div>
                </div>

                <div>
                  <p className="mb-2 text-sm font-medium text-slate-500">
                    Today’s forecast
                  </p>
                  <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                    {todayForecast.map((item) => {
                      const condition = item.weatherCode
                        ? weatherCodeMap[item.weatherCode] || {
                            label: "Weather",
                            icon: "🌤️",
                          }
                        : { label: "Weather", icon: "🌤️" };
                      return (
                        <div
                          key={item.time}
                          className="rounded-2xl border border-slate-200 p-3"
                        >
                          <p className="text-xs text-slate-500">
                            {new Date(item.time).toLocaleTimeString("en-IN", {
                              hour: "numeric",
                            })}
                          </p>
                          <p className="mt-2 text-2xl">{condition.icon}</p>
                          <p className="mt-2 font-semibold text-slate-900">
                            {item.temperature != null
                              ? `${Math.round(item.temperature)}°C`
                              : "—"}
                          </p>
                          <p className="text-xs text-slate-600">
                            {condition.label}
                          </p>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            ) : null}
          </div>

          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">
                Today at a glance
              </h2>
            </div>

            <div className="space-y-3">
              <div className="rounded-2xl bg-emerald-50 p-3 text-sm text-emerald-800">
                <p className="font-semibold">Weather</p>
                <p className="mt-1">{weatherHint}</p>
              </div>
              <div className="rounded-2xl bg-amber-50 p-3 text-sm text-amber-800">
                <p className="font-semibold">Alerts</p>
                <p className="mt-1">
                  {statistics?.openDistressAlerts ?? 0} open alerts tracked for
                  your farm.
                </p>
              </div>
              <div className="rounded-2xl bg-violet-50 p-3 text-sm text-violet-800">
                <p className="font-semibold">Notifications</p>
                <p className="mt-1">
                  {statistics?.unreadNotifications ?? 0} unread messages
                  waiting.
                </p>
              </div>
            </div>
          </div>
        </section>

        <section className="grid gap-6 xl:grid-cols-[1.5fr_1fr]">
          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">My crops</h2>
              <Link
                to="/crops"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                View all
              </Link>
            </div>

            {recentCrops.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No crops added yet
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Start by adding your first crop from My Crops.
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {recentCrops.slice(0, 4).map((crop) => (
                  <div
                    key={crop.id}
                    className="rounded-2xl border border-slate-200 p-4"
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <p className="text-lg font-semibold text-slate-900">
                          {crop.cropName}
                        </p>
                        <p className="text-sm text-slate-500">
                          {crop.cropStage || "Field stage not set"}
                        </p>
                      </div>
                      <span className="rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-medium text-emerald-700">
                        Active
                      </span>
                    </div>
                    <div className="mt-3 grid gap-2 text-sm text-slate-600 sm:grid-cols-2">
                      <p>Planting: {formatDate(crop.sowingDate)}</p>
                      <p>Harvest: {formatDate(crop.expectedHarvestDate)}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">
                Risk & alerts
              </h2>
              <Link
                to="/alerts"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                Open
              </Link>
            </div>

            {latestAlert ? (
              <div className="rounded-2xl border border-slate-200 p-4">
                <div className="flex items-center justify-between gap-3">
                  <p className="text-base font-semibold text-slate-900">
                    {latestAlert.cropName || "Crop alert"}
                  </p>
                  <span
                    className={`rounded-full border px-2.5 py-1 text-xs font-semibold ${riskTone[latestAlert.riskLevel] || riskTone.default}`}
                  >
                    {latestAlert.riskLevel || "UNKNOWN"}
                  </span>
                </div>
                <p className="mt-3 text-sm text-slate-600">
                  {latestAlert.dominantFactor || "Risk factor not specified"}
                </p>
                <p className="mt-2 text-sm text-slate-700">
                  {latestAlert.recommendedAction ||
                    "Follow the field guidance."}
                </p>
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No important alerts
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Your crops look healthy right now.
                </p>
              </div>
            )}

            {recentAlerts.length > 1 && (
              <div className="mt-4 space-y-2">
                {recentAlerts.slice(1, 4).map((alert) => (
                  <div
                    key={alert.id}
                    className="flex items-center justify-between rounded-xl bg-slate-50 p-3 text-sm"
                  >
                    <span className="text-slate-700">
                      {alert.cropName || "Field alert"}
                    </span>
                    <span
                      className={`rounded-full border px-2 py-1 text-[11px] font-semibold ${riskTone[alert.riskLevel] || riskTone.default}`}
                    >
                      {alert.riskLevel || "UNKNOWN"}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        <section className="grid gap-6 xl:grid-cols-3">
          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100 xl:col-span-1">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">
                Crop advisory
              </h2>
              <Link
                to="/advisories"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                Details
              </Link>
            </div>

            {latestAdvisory ? (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
                <p className="text-base font-semibold text-slate-900">
                  {latestAdvisory.cropName || "Latest advice"}
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  {latestAdvisory.recommendationCount ?? 0} recommendations
                  available
                </p>
                <p className="mt-2 text-xs text-slate-500">
                  Updated {formatDate(latestAdvisory.generatedAt)}
                </p>
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No advisory created
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Generate one from the Advisories page.
                </p>
              </div>
            )}
          </div>

          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100 xl:col-span-1">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">
                Market prices
              </h2>
              <Link
                to="/market"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                See all
              </Link>
            </div>

            {marketSummaries.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No market prices yet
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Market data will appear here once crop records are available.
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {marketSummaries.slice(0, 3).map((market) => (
                  <div
                    key={`${market.cropName}-${market.bestMarket}`}
                    className="rounded-2xl border border-slate-200 p-3"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <p className="font-semibold text-slate-900">
                        {market.cropName}
                      </p>
                      <span className="text-xs text-slate-500">
                        {market.marketsTracked ?? 0} markets
                      </span>
                    </div>
                    <p className="mt-2 text-sm text-slate-600">
                      Best market: {market.bestMarket || "—"}
                    </p>
                    <p className="mt-1 text-lg font-bold text-emerald-700">
                      {market.bestModalPrice != null
                        ? `₹${Number(market.bestModalPrice).toFixed(2)}`
                        : "—"}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100 xl:col-span-1">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">Advisories</h2>
              <Link
                to="/advisories"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                All
              </Link>
            </div>

            {latestAdvisory ? (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
                <p className="text-base font-semibold text-slate-900">
                  {latestAdvisory.cropName || "Latest advice"}
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  {latestAdvisory.recommendationCount ?? 0} recommendations
                  available
                </p>
                <p className="mt-2 text-xs text-slate-500">
                  Updated {formatDate(latestAdvisory.generatedAt)}
                </p>
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No advisory created
                </p>
                <p className="mt-2 text-sm text-slate-600">
                  Generate one from the Advisories page.
                </p>
              </div>
            )}
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">
                Important alerts
              </h2>
              <Link
                to="/alerts"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                See all
              </Link>
            </div>

            {recentAlerts.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No recent alerts
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {recentAlerts.slice(0, 4).map((alert) => (
                  <div
                    key={alert.id}
                    className="rounded-2xl border border-slate-200 p-4"
                  >
                    <div className="flex items-center justify-between gap-3">
                      <p className="font-semibold text-slate-900">
                        {alert.cropName || "Alert"}
                      </p>
                      <span
                        className={`rounded-full border px-2 py-1 text-[11px] font-semibold ${riskTone[alert.riskLevel] || riskTone.default}`}
                      >
                        {alert.riskLevel || "UNKNOWN"}
                      </span>
                    </div>
                    <p className="mt-2 text-sm text-slate-600">
                      {alert.dominantFactor || "No factor provided"}
                    </p>
                    <p className="mt-2 text-xs text-slate-500">
                      {formatDate(alert.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-xl font-bold text-slate-900">
                Recent updates
              </h2>
              <Link
                to="/notifications"
                className="text-sm font-medium text-emerald-700 hover:text-emerald-800"
              >
                Open
              </Link>
            </div>

            {unreadNotifications.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                <p className="text-base font-medium text-slate-800">
                  No unread updates
                </p>
              </div>
            ) : (
              <div className="space-y-3">
                {unreadNotifications.slice(0, 4).map((notification) => (
                  <div
                    key={notification.id}
                    className="rounded-2xl border border-slate-200 p-3"
                  >
                    <p className="font-medium text-slate-900">
                      {notification.title || notification.type || "Update"}
                    </p>
                    <p className="mt-1 text-sm text-slate-600">
                      {notification.message || "No message available."}
                    </p>
                    <p className="mt-2 text-xs text-slate-500">
                      {formatDate(notification.createdAt)}
                    </p>
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

export default Dashboard;
