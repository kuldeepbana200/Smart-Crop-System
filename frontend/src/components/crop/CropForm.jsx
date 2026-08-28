import { useState } from 'react';

const STAGES = ['SOWING', 'VEGETATIVE', 'FLOWERING', 'HARVESTING'];
// Placeholder list — confirm the real enum values with your backend teammate.

export default function CropForm({ initialValues, onSubmit, loading, submitLabel }) {
  const [form, setForm] = useState(
    initialValues || { cropName: '', cropStage: 'VEGETATIVE', sowingDate: '', expectedHarvestDate: '' }
  );

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-xl p-8 w-full max-w-sm">
      <input
        name="cropName"
        placeholder="Crop Name (e.g. Rice)"
        value={form.cropName}
        onChange={handleChange}
        className="w-full border rounded-lg px-3 py-2 mb-4"
        required
      />
      <select
        name="cropStage"
        value={form.cropStage}
        onChange={handleChange}
        className="w-full border rounded-lg px-3 py-2 mb-4"
      >
        {STAGES.map((stage) => (
          <option key={stage} value={stage}>{stage}</option>
        ))}
      </select>
      <label className="block text-xs text-gray-500 mb-1">Sowing Date</label>
      <input
        name="sowingDate"
        type="date"
        value={form.sowingDate}
        onChange={handleChange}
        className="w-full border rounded-lg px-3 py-2 mb-4"
        required
      />
      <label className="block text-xs text-gray-500 mb-1">Expected Harvest Date</label>
      <input
        name="expectedHarvestDate"
        type="date"
        value={form.expectedHarvestDate}
        onChange={handleChange}
        className="w-full border rounded-lg px-3 py-2 mb-4"
        required
      />
      <button
        type="submit"
        disabled={loading}
        className="w-full bg-green-600 text-white rounded-lg py-2 font-semibold hover:bg-green-700 disabled:opacity-50"
      >
        {loading ? 'Saving...' : submitLabel}
      </button>
    </form>
  );
}