import { useEffect, useMemo, useState } from "react";
import { cropService, marketService } from "../services/api";

const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0,
});

const isValidMarketPrice = (value) => {
  const numeric = Number(value);
  return Number.isFinite(numeric) && numeric > 0 && numeric < 500000;
};

const getTopMarketPrices = (items = []) => {
  const validItems = items.filter((item) =>
    isValidMarketPrice(item?.modalPrice),
  );
  return [...validItems]
    .sort((a, b) => Number(b.modalPrice) - Number(a.modalPrice))
    .slice(0, 5);
};

const groupPricesByCrop = (items = []) => {
  const grouped = new Map();

  items.forEach((item) => {
    const cropName = (
      item?.commodity ||
      item?.cropName ||
      "Unknown crop"
    ).trim();
    if (!cropName) return;

    const existing = grouped.get(cropName) || [];
    existing.push(item);
    grouped.set(cropName, existing);
  });

  return Array.from(grouped.entries())
    .map(([cropName, cropItems]) => ({
      cropName,
      prices: getTopMarketPrices(cropItems),
    }))
    .filter((group) => group.prices.length > 0);
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

const MarketPrices = () => {
  const [prices, setPrices] = useState([]);
  const [filters, setFilters] = useState({
    cropName: "",
    state: "",
    district: "",
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadPrices = async (nextFilters = filters) => {
    setLoading(true);
    setError("");

    try {
      const hasSearchFilters = Boolean(
        nextFilters.cropName?.trim() ||
        nextFilters.state?.trim() ||
        nextFilters.district?.trim(),
      );

      if (!hasSearchFilters) {
        const { data: crops = [] } = await cropService.getCrops();
        const cropNames = [
          ...new Set(crops.map((crop) => crop.cropName).filter(Boolean)),
        ];

        if (!cropNames.length) {
          setPrices([]);
          return;
        }

        const responses = await Promise.all(
          cropNames.map(async (cropName) => {
            try {
              const { data } = await marketService.getPrices({ cropName });
              return Array.isArray(data) ? data : [];
            } catch {
              return [];
            }
          }),
        );

        const merged = responses.flat();
        setPrices(merged);
        return;
      }

      const { data } = await marketService.getPrices(nextFilters);
      setPrices(data || []);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load market prices right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPrices();
  }, []);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const handleApplyFilters = (event) => {
    event.preventDefault();
    const nextFilters = {
      cropName: filters.cropName?.trim() || undefined,
      state: filters.state?.trim() || undefined,
      district: filters.district?.trim() || undefined,
    };
    loadPrices(nextFilters);
  };

  const clearFilters = () => {
    const reset = { cropName: "", state: "", district: "" };
    setFilters(reset);
    loadPrices({});
  };

  const groupedPrices = useMemo(() => groupPricesByCrop(prices), [prices]);

  const summary = useMemo(() => {
    const modalValues = prices
      .map((item) => Number(item.modalPrice))
      .filter((value) => isValidMarketPrice(value));

    if (!modalValues.length) {
      return { highest: "—", lowest: "—", average: "—" };
    }

    const highest = Math.max(...modalValues);
    const lowest = Math.min(...modalValues);
    const average =
      modalValues.reduce((sum, value) => sum + value, 0) / modalValues.length;

    return {
      highest,
      lowest,
      average,
    };
  }, [prices]);

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
                <div className="mt-4 h-8 w-28 rounded bg-slate-100" />
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
            Unable to load market prices
          </p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={() => loadPrices()}
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
        <section className="rounded-3xl bg-gradient-to-r from-violet-600 to-indigo-500 p-6 text-white shadow-lg shadow-violet-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-violet-100">
            Market prices
          </p>
          <h1 className="mt-2 text-3xl font-bold">Crop price overview</h1>
          <p className="mt-2 text-sm text-violet-50">
            Review market values for crops in your region. Prices shown are from
            the backend response and may vary by market and date.
          </p>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <form
            onSubmit={handleApplyFilters}
            className="grid gap-3 md:grid-cols-4"
          >
            <input
              type="text"
              name="cropName"
              value={filters.cropName}
              onChange={handleFilterChange}
              placeholder="Crop name"
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <input
              type="text"
              name="state"
              value={filters.state}
              onChange={handleFilterChange}
              placeholder="State"
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <input
              type="text"
              name="district"
              value={filters.district}
              onChange={handleFilterChange}
              placeholder="District"
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <div className="flex gap-2">
              <button
                type="submit"
                className="flex-1 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white hover:bg-violet-700"
              >
                Apply
              </button>
              <button
                type="button"
                onClick={clearFilters}
                className="rounded-xl border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                Clear
              </button>
            </div>
          </form>
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Highest modal price</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.highest !== "—"
                ? currencyFormatter.format(summary.highest)
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Lowest modal price</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.lowest !== "—"
                ? currencyFormatter.format(summary.lowest)
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Average modal price</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.average !== "—"
                ? currencyFormatter.format(summary.average)
                : "—"}
            </p>
          </div>
        </section>

        {groupedPrices.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-xl font-semibold text-slate-900">
              No market prices found
            </p>
            <p className="mt-2 text-sm text-slate-600">
              Try another crop name, state, or district to view available market
              prices.
            </p>
          </section>
        ) : (
          <section className="space-y-4">
            {groupedPrices.map(({ cropName, prices: cropPrices }) => (
              <article
                key={cropName}
                className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="text-xl font-bold text-slate-900">
                      {cropName}
                    </p>
                    <p className="text-sm text-slate-500">
                      Top {cropPrices.length} real market prices for this crop
                    </p>
                  </div>
                  <div className="rounded-full bg-violet-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-violet-700">
                    {cropPrices[0]?.unit || "quintal"}
                  </div>
                </div>

                <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                  {cropPrices.map((price, index) => (
                    <div
                      key={`${cropName}-${price.id || index}-${price.market || "market"}`}
                      className="rounded-2xl bg-slate-50 p-4"
                    >
                      <p className="text-xs uppercase tracking-wide text-slate-500">
                        {price.market || "Market"}
                      </p>
                      <p className="mt-2 text-xl font-bold text-slate-900">
                        {isValidMarketPrice(price.modalPrice)
                          ? currencyFormatter.format(price.modalPrice)
                          : "—"}
                      </p>
                      <p className="mt-2 text-xs text-slate-600">
                        {price.district || "District"} •{" "}
                        {price.state || "State"}
                      </p>
                      <p className="mt-1 text-xs text-slate-500">
                        {formatDate(price.arrivalDate)}
                      </p>
                      {(price.variety || price.grade) && (
                        <div className="mt-3 flex flex-wrap gap-2">
                          {price.variety && (
                            <span className="rounded-full bg-white px-2 py-1 text-[10px] text-slate-700 ring-1 ring-slate-200">
                              {price.variety}
                            </span>
                          )}
                          {price.grade && (
                            <span className="rounded-full bg-white px-2 py-1 text-[10px] text-slate-700 ring-1 ring-slate-200">
                              {price.grade}
                            </span>
                          )}
                        </div>
                      )}
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

export default MarketPrices;
