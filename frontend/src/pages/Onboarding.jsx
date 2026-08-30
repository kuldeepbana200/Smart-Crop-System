import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const validationSchema = yup.object({
  district: yup.string().required("District is required"),
  state: yup.string().required("State is required"),
  latitude: yup
    .number()
    .typeError("Latitude must be a number")
    .required("Latitude is required")
    .min(-90, "Invalid latitude")
    .max(90, "Invalid latitude"),
  longitude: yup
    .number()
    .typeError("Longitude must be a number")
    .required("Longitude is required")
    .min(-180, "Invalid longitude")
    .max(180, "Invalid longitude"),
  landArea: yup
    .number()
    .typeError("Land area must be a number")
    .required("Land area is required")
    .min(0, "Land area must be at least 0"),
});

const Onboarding = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    resolver: yupResolver(validationSchema),
    mode: "onTouched",
    defaultValues: {
      district: "",
      state: "",
      latitude: "",
      longitude: "",
      landArea: "",
    },
  });

  useEffect(() => {
    if (!navigator.geolocation) {
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        reset((currentValues) => ({
          ...currentValues,
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        }));
      },
      (err) => {
        console.warn("Could not get current position:", err);
      },
    );
  }, [reset]);

  const onSubmit = async (data) => {
    setLoading(true);
    setError(null);

    try {
      await axios.post(
        "/api/farmers/profile",
        {
          district: data.district,
          state: data.state,
          latitude: Number(data.latitude),
          longitude: Number(data.longitude),
          landArea: Number(data.landArea),
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
            "Content-Type": "application/json",
          },
        },
      );

      navigate("/welcome");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to save profile. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  const inputClass = (fieldError) =>
    `block w-full rounded-md border px-3 py-2 shadow-sm focus:outline-none focus:ring-2 sm:text-sm ${
      fieldError
        ? "border-red-500 focus:border-red-500 focus:ring-red-200"
        : "border-gray-300 focus:border-indigo-500 focus:ring-indigo-200"
    }`;

  return (
    <div className="min-h-screen bg-gray-50 py-6">
      <div className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8">
        <div className="rounded-lg bg-white p-6 shadow-md">
          <h2 className="mb-4 text-2xl font-bold text-gray-900">
            Complete Your Profile
          </h2>

          <p className="mb-6 text-gray-600">
            Help us provide you with better services by filling in your farm
            details.
          </p>

          {error && (
            <div className="mb-6 rounded-md border-l-4 border-red-500 bg-red-50 p-4 text-red-700">
              <p>{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {/* District */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                District
              </label>

              <input
                type="text"
                {...register("district")}
                className={inputClass(errors.district)}
                placeholder="Enter your district"
              />

              {errors.district && (
                <p className="mt-1 text-sm text-red-600">
                  {errors.district.message}
                </p>
              )}
            </div>

            {/* State */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                State
              </label>

              <input
                type="text"
                {...register("state")}
                className={inputClass(errors.state)}
                placeholder="Enter your state"
              />

              {errors.state && (
                <p className="mt-1 text-sm text-red-600">
                  {errors.state.message}
                </p>
              )}
            </div>

            {/* Coordinates */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              {/* Latitude */}
              <div>
                <label className="mb-2 block text-sm font-medium text-gray-700">
                  Latitude
                </label>

                <input
                  type="number"
                  step="any"
                  {...register("latitude")}
                  className={inputClass(errors.latitude)}
                  placeholder="e.g. 28.6139"
                />

                {errors.latitude && (
                  <p className="mt-1 text-sm text-red-600">
                    {errors.latitude.message}
                  </p>
                )}
              </div>

              {/* Longitude */}
              <div>
                <label className="mb-2 block text-sm font-medium text-gray-700">
                  Longitude
                </label>

                <input
                  type="number"
                  step="any"
                  {...register("longitude")}
                  className={inputClass(errors.longitude)}
                  placeholder="e.g. 77.2090"
                />

                {errors.longitude && (
                  <p className="mt-1 text-sm text-red-600">
                    {errors.longitude.message}
                  </p>
                )}
              </div>
            </div>

            {/* Land Area */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                Land Area (in acres)
              </label>

              <input
                type="number"
                step="any"
                {...register("landArea")}
                className={inputClass(errors.landArea)}
                placeholder="Enter your land area"
              />

              {errors.landArea && (
                <p className="mt-1 text-sm text-red-600">
                  {errors.landArea.message}
                </p>
              )}
            </div>

            {/* Buttons */}
            <div className="flex items-center justify-between">
              <button
                type="button"
                onClick={() => navigate("/")}
                className="rounded-md bg-gray-200 px-4 py-2 text-gray-800 hover:bg-gray-300"
              >
                Skip for now
              </button>

              <button
                type="submit"
                disabled={loading}
                className="rounded-md bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {loading ? "Saving..." : "Save Profile"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Onboarding;
