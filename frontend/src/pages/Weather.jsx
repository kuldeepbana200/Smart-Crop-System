import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { weatherService } from "../services/api";

const weatherCodeMap = {
  0: { label: "Clear sky", icon: "☀️" },
  1: { label: "Mainly clear", icon: "🌤️" },
  2: { label: "Partly cloudy", icon: "⛅" },
  3: { label: "Overcast", icon: "☁️" },
  45: { label: "Fog", icon: "🌫️" },
  48: { label: "Depositing rime fog", icon: "🌫️" },
  51: { label: "Light drizzle", icon: "🌦️" },
  53: { label: "Moderate drizzle", icon: "🌦️" },
  55: { label: "Dense drizzle", icon: "🌧️" },
  56: { label: "Freezing drizzle", icon: "🌧️" },
  57: { label: "Heavy freezing drizzle", icon: "🌧️" },
  61: { label: "Slight rain", icon: "🌦️" },
  63: { label: "Moderate rain", icon: "🌧️" },
  65: { label: "Heavy rain", icon: "🌧️" },
  66: { label: "Freezing rain", icon: "🌧️" },
  67: { label: "Heavy freezing rain", icon: "🌧️" },
  71: { label: "Light snow", icon: "🌨️" },
  73: { label: "Moderate snow", icon: "❄️" },
  75: { label: "Heavy snow", icon: "❄️" },
  77: { label: "Snow grains", icon: "❄️" },
  80: { label: "Rain showers", icon: "🌦️" },
  81: { label: "Heavy showers", icon: "🌧️" },
  82: { label: "Violent showers", icon: "⛈️" },
  85: { label: "Snow showers", icon: "🌨️" },
  86: { label: "Heavy snow showers", icon: "🌨️" },
  95: { label: "Thunderstorm", icon: "⛈️" },
  96: { label: "Thunderstorm with hail", icon: "⛈️" },
  99: { label: "Severe thunderstorm", icon: "⛈️" },
};

const formatHour = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("en-IN", {
    hour: "numeric",
    minute: "2-digit",
  }).format(date);
};

const formatDay = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("en-IN", {
    weekday: "short",
    day: "numeric",
    month: "short",
  }).format(date);
};

const resolveCondition = (code) => {
  if (code === null || code === undefined) {
    return { label: "Weather update", icon: "🌤️" };
  }
  return weatherCodeMap[code] || { label: "Weather update", icon: "🌤️" };
};

const Weather = () => {
  const [current, setCurrent] = useState(null);
  const [forecast, setForecast] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadWeather = async () => {
    setLoading(true);
    setError("");

    try {
      const [currentResponse, forecastResponse] = await Promise.all([
        weatherService.getCurrentWeather(),
        weatherService.getForecast(),
      ]);

      setCurrent(currentResponse?.data || null);
      setForecast(forecastResponse?.data || null);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load weather data right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWeather();
  }, []);

  const weatherCondition = useMemo(
    () => resolveCondition(current?.weatherCode),
    [current],
  );

  const hourlyForecast = forecast?.hourly || {};
  const dailyForecast = forecast?.daily || {};

  if (loading) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-7xl space-y-4">
          <div className="animate-pulse rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
            <div className="h-6 w-40 rounded bg-slate-100" />
            <div className="mt-4 h-10 w-72 rounded bg-slate-100" />
          </div>
          <div className="grid gap-4 md:grid-cols-3">
            {[0, 1, 2].map((item) => (
              <div
                key={item}
                className="animate-pulse rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="h-4 w-20 rounded bg-slate-100" />
                <div className="mt-4 h-8 w-24 rounded bg-slate-100" />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    const missingLocation =
      error.toLowerCase().includes("coordinates") ||
      error.toLowerCase().includes("location");

    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-white p-6 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">
            {missingLocation
              ? "Add your farm location"
              : "Unable to load weather"}
          </p>
          <p className="mt-2 text-sm text-slate-600">
            {missingLocation
              ? "Save your field location in the profile page so Smart Crop can show weather for your farm."
              : error}
          </p>
          {missingLocation ? (
            <Link
              to="/profile"
              className="mt-5 inline-flex rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
            >
              Update my location
            </Link>
          ) : (
            <button
              type="button"
              onClick={loadWeather}
              className="mt-5 rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-emerald-700"
            >
              Try again
            </button>
          )}
        </div>
      </div>
    );
  }

  if (!current && !forecast) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl rounded-2xl border border-dashed border-slate-200 bg-white p-6 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">
            No weather data available
          </p>
          <p className="mt-2 text-sm text-slate-600">
            Weather details will appear here once your field location is
            available.
          </p>
        </div>
      </div>
    );
  }

  const currentTimestamp = current?.timestamp || current?.time || "";

  return (
    <div className="min-h-screen bg-emerald-50 px-4 py-6">
      <div className="mx-auto max-w-7xl space-y-6">
        <section className="rounded-3xl bg-gradient-to-r from-sky-600 to-emerald-500 p-6 text-white shadow-lg shadow-sky-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-sky-100">
            Weather
          </p>
          <div className="mt-4 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <h1 className="text-3xl font-bold">
                {weatherCondition.icon} {weatherCondition.label}
              </h1>
              <p className="mt-2 text-sm text-sky-100">
                {currentTimestamp
                  ? `Updated ${formatHour(currentTimestamp)}`
                  : "Updated recently"}
              </p>
            </div>
            <div className="rounded-2xl bg-white/10 p-4 text-left ring-1 ring-white/20 backdrop-blur-sm">
              <p className="text-xs uppercase tracking-[0.2em] text-sky-100">
                Now
              </p>
              <p className="mt-2 text-4xl font-bold">
                {current?.temperature != null
                  ? `${Math.round(current.temperature)}°C`
                  : "—"}
              </p>
            </div>
          </div>
        </section>

        <section className="grid gap-4 md:grid-cols-4">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Humidity</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {current?.relativeHumidity != null
                ? `${Math.round(current.relativeHumidity)}%`
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Rain</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {current?.precipitation != null
                ? `${current.precipitation.toFixed(1)} mm`
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Wind</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {current?.windSpeed != null
                ? `${current.windSpeed.toFixed(1)} km/h`
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Timezone</p>
            <p className="mt-3 text-lg font-bold text-slate-900">
              {forecast?.timezone || current?.timezone || "—"}
            </p>
          </div>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-900">
              Hourly forecast
            </h2>
            <span className="text-sm text-slate-500">Next 24 hours</span>
          </div>

          {hourlyForecast.timestamps?.length ? (
            <div className="grid gap-3 md:grid-cols-3 xl:grid-cols-6">
              {hourlyForecast.timestamps.slice(0, 12).map((time, index) => {
                const code = hourlyForecast.weatherCode?.[index];
                const condition = resolveCondition(code);
                const temp = hourlyForecast.temperature?.[index];
                const rain = hourlyForecast.precipitation?.[index];

                return (
                  <div
                    key={`${time}-${index}`}
                    className="rounded-2xl border border-slate-200 p-4"
                  >
                    <p className="text-sm font-medium text-slate-500">
                      {formatHour(time)}
                    </p>
                    <p className="mt-3 text-2xl">{condition.icon}</p>
                    <p className="mt-2 text-lg font-semibold text-slate-900">
                      {temp != null ? `${Math.round(temp)}°C` : "—"}
                    </p>
                    <p className="mt-1 text-xs text-slate-600">
                      {condition.label}
                    </p>
                    <p className="mt-3 text-xs text-slate-500">
                      {rain != null
                        ? `Rain ${rain.toFixed(1)} mm`
                        : "No rain data"}
                    </p>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center text-sm text-slate-600">
              Hourly forecast is not available right now.
            </div>
          )}
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-900">Weekly outlook</h2>
            <span className="text-sm text-slate-500">7 day forecast</span>
          </div>

          {dailyForecast.timestamps?.length ? (
            <div className="space-y-3">
              {dailyForecast.timestamps.map((day, index) => {
                const code = dailyForecast.weatherCode?.[index];
                const condition = resolveCondition(code);
                const max = dailyForecast.temperatureMax?.[index];
                const min = dailyForecast.temperatureMin?.[index];
                const rain = dailyForecast.precipitationSum?.[index];

                return (
                  <div
                    key={`${day}-${index}`}
                    className="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 p-4"
                  >
                    <div className="flex items-center gap-3">
                      <span className="text-2xl">{condition.icon}</span>
                      <div>
                        <p className="font-semibold text-slate-900">
                          {formatDay(day)}
                        </p>
                        <p className="text-sm text-slate-500">
                          {condition.label}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-6 text-sm text-slate-600">
                      <span>{max != null ? `${Math.round(max)}°` : "—"}</span>
                      <span>{min != null ? `${Math.round(min)}°` : "—"}</span>
                      <span>
                        {rain != null ? `${rain.toFixed(1)} mm` : "—"}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-6 text-center text-sm text-slate-600">
              Weekly forecast is not available right now.
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default Weather;
