import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext";
import {
  cropService,
  farmerService,
  marketAdviceService,
  marketService,
} from "../services/api";

const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0,
});

const labelsByLanguage = {
  en: {
    marketPrices: "Market prices",
    cropOverview: "Crop price overview",
    subtitle:
      "Review market values for crops in your region. Prices shown are backend-driven and may vary by market and date.",
    myCrops: "My Crops",
    cropName: "Crop name",
    state: "State",
    district: "District",
    apply: "Apply",
    clear: "Clear",
    highest: "Highest modal price",
    lowest: "Lowest modal price",
    average: "Average modal price",
    noPrices: "No market prices found",
    noPricesHint:
      "Try another crop name, state, or district to view available market prices.",
    topRealPrices: "Top real market prices for this crop",
    bestAvailable: "Best available price",
    market: "Market",
    location: "Location",
    unit: "Unit",
    date: "Date",
    trend: "Trend",
    compare: "Market comparison",
    aiInsight: "AI Market Insight",
    aiGenerated: "AI-generated from available market data",
    noInsight: "AI market insight is not available right now.",
    retry: "Try again",
    loading: "Loading market data...",
    noCrops: "No crops are currently added to your profile.",
    farmLocation: "Farm location",
    compareHint: "Real backend market comparison for this crop",
    noComparison: "No comparison data is available for this crop yet.",
    noTrend: "Trend data is unavailable for this crop yet.",
    disclaimer:
      "AI-generated market estimates — verify with your local mandi before selling.",
  },
  hi: {
    marketPrices: "बाजार कीमतें",
    cropOverview: "फसल कीमत अवलोकन",
    subtitle:
      "अपने क्षेत्र की फसलों की बाजार कीमतें देखें। कीमतें बैकएंड से ली गई हैं और बाजार/तारीख के अनुसार बदल सकती हैं।",
    myCrops: "मेरी फसलें",
    cropName: "फसल का नाम",
    state: "राज्य",
    district: "जिला",
    apply: "लागू करें",
    clear: "साफ करें",
    highest: "सबसे अधिक मोडल कीमत",
    lowest: "सबसे कम मोडल कीमत",
    average: "औसत मोडल कीमत",
    noPrices: "कोई बाजार कीमत नहीं मिली",
    noPricesHint: "दूसरा फसल नाम, राज्य या जिला आज़माएँ।",
    topRealPrices: "इस फसल की शीर्ष वास्तविक बाजार कीमतें",
    bestAvailable: "सबसे अच्छी उपलब्ध कीमत",
    market: "बाजार",
    location: "स्थान",
    unit: "इकाई",
    date: "तारीख",
    trend: "रुझान",
    compare: "बाजार तुलना",
    aiInsight: "एआई बाजार अंतर्दृष्टि",
    aiGenerated: "उपलब्ध बाजार डेटा से एआई जनित",
    noInsight: "एआई बाजार अंतर्दृष्टि अभी उपलब्ध नहीं है।",
    retry: "फिर से प्रयास करें",
    loading: "बाजार डेटा लोड हो रहा है...",
    noCrops: "आपके प्रोफ़ाइल में अभी कोई फसल नहीं है।",
    farmLocation: "खेत का स्थान",
    compareHint: "इस फसल के लिए वास्तविक बैकएंड बाजार तुलना",
    noComparison: "इस फसल के लिए तुलना डेटा उपलब्ध नहीं है।",
    noTrend: "इस फसल के लिए रुझान डेटा उपलब्ध नहीं है।",
    disclaimer:
      "एआई-जनित बाजार अनुमान — बेचने से पहले अपने स्थानीय मंडी से सत्यापित करें।",
  },
  or: {
    marketPrices: "ବଜାର ମୂଲ୍ୟ",
    cropOverview: "ଫସଲ ମୂଲ୍ୟ ସମୀକ୍ଷା",
    subtitle:
      "ଆପଣଙ୍କ ସ୍ଥାନର ଫସଲଗୁଡ଼ିକର ବଜାର ମୂଲ୍ୟ ଦେଖନ୍ତୁ। ମୂଲ୍ୟଗୁଡ଼ିକ ବ୍ୟାକଏଣ୍ଡ ଦ୍ୱାରା ହାସ୍ତାଚ୍ଛନ୍ତ ଏବଂ ବଜାର/ତାରିଖ ଅନୁସାରେ ବଦଳିପାରେ।",
    myCrops: "ମୋର ଫସଲ",
    cropName: "ଫସଲ ନାମ",
    state: "ରାଜ୍ୟ",
    district: "ଜିଲ୍ଲା",
    apply: "ପ୍ରୟୋଗ କରନ୍ତୁ",
    clear: "ସଫା କରନ୍ତୁ",
    highest: "ସବୁଠାରୁ ଅଧିକ ମୋଡେଲ ମୂଲ୍ୟ",
    lowest: "ସବୁଠାରୁ କମ୍ ମୋଡେଲ ମୂଲ୍ୟ",
    average: "ଔଷଧି ମୋଡେଲ ମୂଲ୍ୟ",
    noPrices: "କୌଣସି ବଜାର ମୂଲ୍ୟ ମିଳିଲା ନାହିଁ",
    noPricesHint: "ଅନ୍ୟ ଫସଲ ନାମ, ରାଜ୍ୟ, କିମ୍ବା ଜିଲ୍ଲା ଚେଷ୍ଟା କରନ୍ତୁ।",
    topRealPrices: "ଏହି ଫସଲର ସର୍ବୋଚ୍ଚ ବାସ୍ତବିକ ବଜାର ମୂଲ୍ୟ",
    bestAvailable: "ସବୁଠାରୁ ଉତ୍ତମ ଉପଲବ୍ଧ ମୂଲ୍ୟ",
    market: "ବଜାର",
    location: "ସ୍ଥାନ",
    unit: "ଏକକ",
    date: "ତାରିଖ",
    trend: "ପ୍ରବୃତ୍ତି",
    compare: "ବଜାର ତୁଳନା",
    aiInsight: "AI ବଜାର ଅନ୍ତର୍ଦୃଷ୍ଟି",
    aiGenerated: "ଉପଲବ୍ଧ ବଜାର ତଥ୍ୟ ଦ୍ୱାରା AI ଜନିତ",
    noInsight: "AI ବଜାର ଅନ୍ତର୍ଦୃଷ୍ଟି ଏବେ ଉପଲବ୍ଧ ନାହିଁ।",
    retry: "ପୁନ୍ରାୟାସ କରନ୍ତୁ",
    loading: "ବଜାର ତଥ୍ୟ ଲୋଡ୍ ହେଉଛି...",
    noCrops: "ଆପଣଙ୍କ ପ୍ରୋଫାଇଲରେ ବର୍ତ୍ତମାନ କୌଣସି ଫସଲ ନାହିଁ।",
    farmLocation: "କ୍ଷେତ୍ର ସ୍ଥାନ",
    compareHint: "ଏହି ଫସଲର ବାସ୍ତବିକ ବ୍ୟାକଏଣ୍ଡ ବଜାର ତୁଳନା",
    noComparison: "ଏହି ଫସଲ ପାଇଁ ତୁଳନା ତଥ୍ୟ ଉପଲବ୍ଧ ନାହିଁ।",
    noTrend: "ଏହି ଫସଲ ପାଇଁ ପ୍ରବୃତ୍ତି ତଥ୍ୟ ଉପଲବ୍ଧ ନାହିଁ।",
    disclaimer:
      "AI-ଜନିତ ବଜାର ଆନୁମାନ — ବେଚିବା ପୂର୍ବରୁ ଆପଣଙ୍କ ସ୍ଥାନୀୟ ମଣ୍ଡି ଦ୍ୱାରା ଯାଞ୍ଚ କରନ୍ତୁ।",
  },
  mr: {
    marketPrices: "बाजार भाव",
    cropOverview: "पिके भाव overview",
    subtitle:
      "तुमच्या परिसरातील पिकांच्या बाजार भावांचे निरीक्षण करा. भाव हे बॅकएंड-आधारित आहेत आणि बाजार/तारीखानुसार बदलू शकतात.",
    myCrops: "माझ्या पिके",
    cropName: "पिकाचे नाव",
    state: "राज्य",
    district: "जिल्हा",
    apply: "लागू करा",
    clear: "पुसून टाका",
    highest: "सर्वात जास्त मोडल भाव",
    lowest: "सर्वात कमी मोडल भाव",
    average: "सरासरी मोडल भाव",
    noPrices: "कोणताही बाजार भाव सापडला नाही",
    noPricesHint: "दुसरे पीक नाव, राज्य किंवा जिल्हा वापरून पहा.",
    topRealPrices: "या पिकासाठी सर्वोच्च वास्तविक बाजार भाव",
    bestAvailable: "उपलब्ध सर्वोत्तम भाव",
    market: "बाजार",
    location: "स्थान",
    unit: "एकक",
    date: "तारीख",
    trend: "प्रवृत्ती",
    compare: "बाजार तुलना",
    aiInsight: "AI बाजार अंतर्दृष्टी",
    aiGenerated: "उपलब्ध बाजार डेटावरून AI द्वारे तयार केलेले",
    noInsight: "AI बाजार अंतर्दृष्टी उपलब्ध नाही.",
    retry: "पुन्हा प्रयत्न करा",
    loading: "बाजार डेटा लोड होत आहे...",
    noCrops: "तुमच्या प्रोफाइलमध्ये सध्या कोणतीही पिके नाहीत.",
    farmLocation: "शेताचे स्थान",
    compareHint: "या पिकासाठी वास्तविक बॅकएंड बाजार तुलना",
    noComparison: "या पिकासाठी तुलना डेटा उपलब्ध नाही.",
    noTrend: "या पिकासाठी प्रवृत्ती डेटा उपलब्ध नाही.",
    disclaimer:
      "AI-जनित बाजार अंदाज — विक्री करण्यापूर्वी तुमच्या स्थानिक मंडीमध्ये पडताळणी करा.",
  },
};

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
  const { user } = useAuth();
  const preferredLanguage =
    user?.preferredLanguage || localStorage.getItem("language") || "en";
  const labels = labelsByLanguage[preferredLanguage] || labelsByLanguage.en;

  const [prices, setPrices] = useState([]);
  const [myCrops, setMyCrops] = useState([]);
  const [farmerProfile, setFarmerProfile] = useState(null);
  const [marketAdvice, setMarketAdvice] = useState([]);
  const [priceHistory, setPriceHistory] = useState([]);
  const [comparison, setComparison] = useState([]);
  const [filters, setFilters] = useState({
    cropName: "",
    state: "",
    district: "",
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [insightLoading, setInsightLoading] = useState(false);
  const [insightError, setInsightError] = useState("");

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

  const loadMarketInsights = async () => {
    setInsightLoading(true);
    setInsightError("");

    try {
      const { data } = await marketAdviceService.getMarketAdvice();
      setMarketAdvice(Array.isArray(data) ? data : []);
    } catch (err) {
      const message =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        "Unable to load AI market insight.";
      setInsightError(message);
      setMarketAdvice([]);
    } finally {
      setInsightLoading(false);
    }
  };

  const loadMarketDetails = async (cropName, state, district) => {
    if (!cropName) {
      setPriceHistory([]);
      setComparison([]);
      return;
    }

    try {
      const [historyResponse, compareResponse] = await Promise.allSettled([
        marketService.getPriceHistory({
          cropName,
          state: state || undefined,
          startDate: undefined,
          endDate: undefined,
        }),
        marketService.comparePrices({
          cropName,
          state: state || undefined,
          district: district || undefined,
        }),
      ]);

      if (historyResponse.status === "fulfilled") {
        setPriceHistory(
          Array.isArray(historyResponse.value?.data)
            ? historyResponse.value.data
            : [],
        );
      } else {
        setPriceHistory([]);
      }

      if (compareResponse.status === "fulfilled") {
        setComparison(
          Array.isArray(compareResponse.value?.data)
            ? compareResponse.value.data
            : [],
        );
      } else {
        setComparison([]);
      }
    } catch {
      setPriceHistory([]);
      setComparison([]);
    }
  };

  useEffect(() => {
    const initialize = async () => {
      try {
        const [profileResponse, cropsResponse] = await Promise.all([
          farmerService.getProfile().catch(() => null),
          cropService.getCrops().catch(() => []),
        ]);

        const profile = profileResponse?.data || null;
        const cropList = Array.isArray(cropsResponse?.data)
          ? cropsResponse.data
          : [];
        const profileCropName = cropList[0]?.cropName || "";
        const defaultCropName = profileCropName || "";
        const defaultState = profile?.state || "";
        const defaultDistrict = profile?.district || "";

        setFarmerProfile(profile);
        setMyCrops(cropList);

        const nextFilters = {
          cropName: defaultCropName,
          state: defaultState,
          district: defaultDistrict,
        };

        setFilters(nextFilters);
        if (defaultCropName) {
          await loadPrices(nextFilters);
          await loadMarketDetails(
            defaultCropName,
            defaultState,
            defaultDistrict,
          );
        } else {
          await loadPrices({
            cropName: undefined,
            state: undefined,
            district: undefined,
          });
          await loadMarketDetails(undefined, undefined, undefined);
        }

        await loadMarketInsights();
      } catch {
        setError("Unable to load market context right now.");
        setLoading(false);
      }
    };

    initialize();
  }, []);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters((current) => ({ ...current, [name]: value }));
  };

  const handleApplyFilters = async (event) => {
    event.preventDefault();
    const nextFilters = {
      cropName: filters.cropName?.trim() || undefined,
      state: filters.state?.trim() || undefined,
      district: filters.district?.trim() || undefined,
    };
    await loadPrices(nextFilters);
    await loadMarketDetails(
      nextFilters.cropName,
      nextFilters.state,
      nextFilters.district,
    );
  };

  const clearFilters = async () => {
    const reset = { cropName: "", state: "", district: "" };
    setFilters(reset);
    await loadPrices({});
    await loadMarketDetails(undefined, undefined, undefined);
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

    return { highest, lowest, average };
  }, [prices]);

  const hasTrendData = priceHistory.length > 0;
  const hasComparisonData = comparison.length > 0;

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
            {labels.retry}
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
            {labels.marketPrices}
          </p>
          <h1 className="mt-2 text-3xl font-bold">{labels.cropOverview}</h1>
          <p className="mt-2 text-sm text-violet-50">{labels.subtitle}</p>
          <div className="mt-4 inline-flex rounded-full border border-white/30 bg-white/10 px-3 py-1.5 text-xs font-medium text-violet-50 backdrop-blur-sm">
            {labels.disclaimer}
          </div>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="mb-4 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm font-semibold text-slate-900">
                {labels.myCrops}
              </p>
              {farmerProfile ? (
                <p className="text-xs text-slate-500">
                  {labels.farmLocation}: {farmerProfile.district || "—"},{" "}
                  {farmerProfile.state || "—"}
                </p>
              ) : null}
            </div>
            {myCrops.length ? (
              <div className="flex flex-wrap gap-2">
                {myCrops.slice(0, 5).map((crop) => (
                  <button
                    key={crop.id || crop.cropName}
                    type="button"
                    onClick={() =>
                      setFilters((current) => ({
                        ...current,
                        cropName: crop.cropName || "",
                      }))
                    }
                    className={`rounded-full px-3 py-1.5 text-xs font-medium ${
                      filters.cropName === crop.cropName
                        ? "bg-emerald-600 text-white"
                        : "bg-emerald-50 text-emerald-700"
                    }`}
                  >
                    {crop.cropName}
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-500">{labels.noCrops}</p>
            )}
          </div>

          <form
            onSubmit={handleApplyFilters}
            className="grid gap-3 md:grid-cols-4"
          >
            <input
              type="text"
              name="cropName"
              value={filters.cropName}
              onChange={handleFilterChange}
              placeholder={labels.cropName}
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <input
              type="text"
              name="state"
              value={filters.state}
              onChange={handleFilterChange}
              placeholder={labels.state}
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <input
              type="text"
              name="district"
              value={filters.district}
              onChange={handleFilterChange}
              placeholder={labels.district}
              className="rounded-xl border border-slate-300 px-3 py-3 text-sm text-slate-900 outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
            />
            <div className="flex gap-2">
              <button
                type="submit"
                className="flex-1 rounded-xl bg-violet-600 px-4 py-3 text-sm font-semibold text-white hover:bg-violet-700"
              >
                {labels.apply}
              </button>
              <button
                type="button"
                onClick={clearFilters}
                className="rounded-xl border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                {labels.clear}
              </button>
            </div>
          </form>
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">{labels.highest}</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.highest !== "—"
                ? currencyFormatter.format(summary.highest)
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">{labels.lowest}</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.lowest !== "—"
                ? currencyFormatter.format(summary.lowest)
                : "—"}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">{labels.average}</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {summary.average !== "—"
                ? currencyFormatter.format(summary.average)
                : "—"}
            </p>
          </div>
        </section>

        <section className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-lg font-bold text-slate-900">
                {labels.aiInsight}
              </p>
              <p className="text-xs text-slate-500">{labels.aiGenerated}</p>
            </div>
            {insightLoading ? (
              <span className="text-xs text-slate-500">{labels.loading}</span>
            ) : null}
          </div>

          {insightError ? (
            <div className="mt-4 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              {insightError}
              <button
                type="button"
                onClick={loadMarketInsights}
                className="ml-3 font-semibold underline"
              >
                {labels.retry}
              </button>
            </div>
          ) : marketAdvice.length ? (
            <div className="mt-4 space-y-4">
              {marketAdvice.map((item) => (
                <article
                  key={`${item.crop}-${item.summary}`}
                  className="rounded-2xl border border-slate-200 bg-slate-50 p-4"
                >
                  <div className="flex items-center justify-between gap-3">
                    <p className="text-lg font-semibold text-slate-900">
                      {item.crop || "Crop"}
                    </p>
                    <span className="rounded-full bg-emerald-100 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-emerald-700">
                      {labels.aiGenerated}
                    </span>
                  </div>
                  <p className="mt-3 text-sm text-slate-700">{item.summary}</p>
                  <p className="mt-3 text-sm text-slate-700">
                    <span className="font-semibold">{labels.trend}:</span>{" "}
                    {item.trend}
                  </p>
                  <p className="mt-2 text-sm text-slate-700">
                    <span className="font-semibold">{labels.apply}:</span>{" "}
                    {item.advice}
                  </p>
                  <p className="mt-2 text-sm text-amber-700">
                    <span className="font-semibold">Caution:</span>{" "}
                    {item.caution}
                  </p>
                </article>
              ))}
            </div>
          ) : (
            <div className="mt-4 rounded-2xl border border-dashed border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
              {labels.noInsight}
            </div>
          )}
        </section>

        {hasTrendData || hasComparisonData ? (
          <section className="grid gap-4 xl:grid-cols-2">
            {hasTrendData ? (
              <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                <p className="text-lg font-bold text-slate-900">
                  {labels.trend}
                </p>
                <div className="mt-4 space-y-3">
                  {priceHistory.slice(0, 5).map((item, index) => (
                    <div
                      key={`${item.market || "market"}-${item.arrivalDate || index}`}
                      className="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2 text-sm text-slate-700"
                    >
                      <span>{formatDate(item.arrivalDate)}</span>
                      <span className="font-semibold text-slate-900">
                        {currencyFormatter.format(item.modalPrice)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            ) : null}

            {hasComparisonData ? (
              <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
                <p className="text-lg font-bold text-slate-900">
                  {labels.compare}
                </p>
                <p className="mt-1 text-xs text-slate-500">
                  {labels.compareHint}
                </p>
                <div className="mt-4 space-y-3">
                  {comparison.slice(0, 5).map((item, index) => (
                    <div
                      key={`${item.market || "compare"}-${item.district || "district"}-${index}`}
                      className="flex items-center justify-between rounded-xl bg-slate-50 px-3 py-2 text-sm text-slate-700"
                    >
                      <div>
                        <p className="font-semibold text-slate-900">
                          {item.market || "Market"}
                        </p>
                        <p className="text-xs text-slate-500">
                          {item.district || "District"}, {item.state || "State"}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold text-slate-900">
                          {currencyFormatter.format(item.modalPrice)}
                        </p>
                        <p className="text-xs text-slate-500">
                          {item.unit || "quintal"}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : null}
          </section>
        ) : null}

        {groupedPrices.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-xl font-semibold text-slate-900">
              {labels.noPrices}
            </p>
            <p className="mt-2 text-sm text-slate-600">{labels.noPricesHint}</p>
          </section>
        ) : (
          <section className="space-y-4">
            {groupedPrices.map(({ cropName, prices: cropPrices }) => {
              const bestPrice = cropPrices.reduce((best, current) => {
                if (!best) return current;
                return Number(current.modalPrice) > Number(best.modalPrice)
                  ? current
                  : best;
              }, null);

              return (
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
                        {labels.topRealPrices}
                      </p>
                    </div>
                    <div className="rounded-full bg-violet-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-violet-700">
                      {cropPrices[0]?.unit || "quintal"}
                    </div>
                  </div>

                  <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                    {cropPrices.map((price, index) => {
                      const isBestAvailable =
                        bestPrice &&
                        `${price.market || "market"}-${price.district || "district"}-${price.state || "state"}-${price.arrivalDate || index}` ===
                          `${bestPrice.market || "market"}-${bestPrice.district || "district"}-${bestPrice.state || "state"}-${bestPrice.arrivalDate || index}`;

                      return (
                        <div
                          key={`${cropName}-${price.id || index}-${price.market || "market"}`}
                          className={`rounded-2xl p-4 ${
                            isBestAvailable
                              ? "border-2 border-emerald-300 bg-emerald-50 shadow-sm"
                              : "bg-slate-50"
                          }`}
                        >
                          <div className="flex items-center justify-between gap-2">
                            <p className="text-xs uppercase tracking-wide text-slate-500">
                              {price.market || "Market"}
                            </p>
                            {isBestAvailable ? (
                              <span className="rounded-full bg-emerald-100 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-emerald-700">
                                {labels.bestAvailable}
                              </span>
                            ) : null}
                          </div>
                          <p className="mt-2 text-xl font-bold text-slate-900">
                            {isValidMarketPrice(price.modalPrice)
                              ? currencyFormatter.format(price.modalPrice)
                              : "—"}
                          </p>
                          <p className="mt-2 text-xs text-slate-600">
                            {labels.location}: {price.district || "District"} •{" "}
                            {price.state || "State"}
                          </p>
                          <p className="mt-1 text-xs text-slate-500">
                            {labels.unit}: {price.unit || "quintal"}
                          </p>
                          <p className="mt-1 text-xs text-slate-500">
                            {labels.date}: {formatDate(price.arrivalDate)}
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
                      );
                    })}
                  </div>
                </article>
              );
            })}
          </section>
        )}
      </div>
    </div>
  );
};

export default MarketPrices;
