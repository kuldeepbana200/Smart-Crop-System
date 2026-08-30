import { useEffect, useMemo, useState } from "react";
import { farmerService } from "../services/api";
import LanguageSettings from "./LanguageSettings";

const emptyForm = {
  district: "",
  state: "",
  latitude: "",
  longitude: "",
  landArea: "",
};

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [locationLoading, setLocationLoading] = useState(false);
  const [error, setError] = useState("");

  const loadProfile = async () => {
    setLoading(true);
    setError("");

    try {
      const { data } = await farmerService.getProfile();
      setProfile(data || null);
      setForm({
        district: data?.district || "",
        state: data?.state || "",
        latitude: data?.latitude ?? "",
        longitude: data?.longitude ?? "",
        landArea: data?.landArea ?? "",
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load your farm profile right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError(
        "GPS is not available on this device. Please enter coordinates manually.",
      );
      return;
    }

    setLocationLoading(true);
    setError("");

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setForm((current) => ({
          ...current,
          latitude: position.coords.latitude.toString(),
          longitude: position.coords.longitude.toString(),
        }));
        setLocationLoading(false);
      },
      () => {
        setError(
          "We could not access your current location. Please enter the coordinates manually.",
        );
        setLocationLoading(false);
      },
      { enableHighAccuracy: true, timeout: 20000 },
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      const payload = {
        district: form.district,
        state: form.state,
        latitude: form.latitude === "" ? null : Number(form.latitude),
        longitude: form.longitude === "" ? null : Number(form.longitude),
        landArea: form.landArea === "" ? null : Number(form.landArea),
      };

      const { data } = await farmerService.updateProfile(payload);
      setProfile(data || null);
      setForm({
        district: data?.district || "",
        state: data?.state || "",
        latitude: data?.latitude ?? "",
        longitude: data?.longitude ?? "",
        landArea: data?.landArea ?? "",
      });
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to save your profile. Please try again.",
      );
    } finally {
      setSaving(false);
    }
  };

  const summary = useMemo(() => {
    if (!profile) {
      return { location: "Not set", area: "0 ha" };
    }

    return {
      location: `${profile.district || "District"}, ${profile.state || "State"}`,
      area: profile.landArea != null ? `${profile.landArea} ha` : "Not set",
    };
  }, [profile]);

  if (loading) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-7xl space-y-4">
          <div className="animate-pulse rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
            <div className="h-6 w-40 rounded bg-slate-100" />
            <div className="mt-4 h-10 w-72 rounded bg-slate-100" />
          </div>
          <div className="animate-pulse rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
            <div className="h-40 w-full rounded bg-slate-100" />
          </div>
        </div>
      </div>
    );
  }

  if (error && !profile) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-3xl rounded-2xl border border-red-200 bg-white p-6 text-center shadow-sm">
          <p className="text-lg font-semibold text-slate-900">
            Unable to load your profile
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={loadProfile}
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
      <div className="mx-auto max-w-5xl space-y-6">
        <section className="rounded-3xl bg-gradient-to-r from-emerald-600 to-teal-500 p-6 text-white shadow-lg shadow-emerald-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-emerald-100">
            Profile
          </p>
          <h1 className="mt-2 text-3xl font-bold">Farm profile</h1>
          <p className="mt-2 text-sm text-emerald-50">
            Manage your location, land area, and field details.
          </p>
        </section>

        <section className="grid gap-4 md:grid-cols-2">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Farmer</p>
            <p className="mt-3 text-2xl font-bold text-slate-900">
              {profile?.name || "Farmer"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Location</p>
            <p className="mt-3 text-2xl font-bold text-slate-900">
              {summary.location}
            </p>
          </div>
        </section>

        <form
          onSubmit={handleSubmit}
          className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
        >
          <div className="mb-5 flex items-center justify-between gap-3">
            <h2 className="text-xl font-bold text-slate-900">Farm details</h2>
            <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
              {summary.area}
            </span>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                District
              </label>
              <input
                name="district"
                value={form.district}
                onChange={handleChange}
                className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                placeholder="District"
              />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">
                State
              </label>
              <input
                name="state"
                value={form.state}
                onChange={handleChange}
                className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                placeholder="State"
              />
            </div>
            <div className="md:col-span-2">
              <div className="mb-2 flex items-center justify-between gap-3">
                <label className="block text-sm font-medium text-slate-700">
                  GPS coordinates
                </label>
                <button
                  type="button"
                  onClick={handleUseCurrentLocation}
                  disabled={locationLoading}
                  className="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-700 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {locationLoading ? "Detecting..." : "Use my current location"}
                </button>
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">
                    Latitude
                  </label>
                  <input
                    type="number"
                    step="0.0001"
                    name="latitude"
                    value={form.latitude}
                    onChange={handleChange}
                    className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                    placeholder="20.5937"
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-700">
                    Longitude
                  </label>
                  <input
                    type="number"
                    step="0.0001"
                    name="longitude"
                    value={form.longitude}
                    onChange={handleChange}
                    className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                    placeholder="78.9629"
                  />
                </div>
              </div>
            </div>
            <div className="md:col-span-2">
              <label className="mb-2 block text-sm font-medium text-slate-700">
                Land area (hectares)
              </label>
              <input
                type="number"
                step="0.1"
                name="landArea"
                value={form.landArea}
                onChange={handleChange}
                className="w-full rounded-xl border border-slate-300 px-3 py-3 text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                placeholder="5.5"
              />
            </div>
          </div>

          {error && (
            <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={saving}
            className="mt-5 w-full rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white shadow-sm hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {saving ? "Saving profile..." : "Save profile"}
          </button>
        </form>

        <div className="pt-2">
          <LanguageSettings />
        </div>
      </div>
    </div>
  );
};

export default Profile;
