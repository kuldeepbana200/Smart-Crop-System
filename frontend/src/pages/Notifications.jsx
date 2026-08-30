import { useEffect, useMemo, useState } from "react";
import { notificationService } from "../services/api";

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

const Notifications = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [readCount, setReadCount] = useState(0);

  const loadNotifications = async () => {
    setLoading(true);
    setError("");

    try {
      const [notificationsRes, unreadRes] = await Promise.all([
        notificationService.getNotifications(),
        notificationService.getUnreadCount(),
      ]);

      const notifications = notificationsRes?.data || [];
      setItems(notifications);
      setReadCount(unreadRes?.data?.length || 0);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to load notifications right now.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const unread = useMemo(
    () => items.filter((item) => item.status === "UNREAD").length,
    [items],
  );

  const markRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      await loadNotifications();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          err.message ||
          "Unable to update the notification status.",
      );
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-emerald-50 px-4 py-6">
        <div className="mx-auto max-w-7xl space-y-4">
          <div className="animate-pulse rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-100">
            <div className="h-6 w-40 rounded bg-slate-100" />
            <div className="mt-4 h-10 w-72 rounded bg-slate-100" />
          </div>
          <div className="space-y-3">
            {[0, 1, 2].map((item) => (
              <div
                key={item}
                className="animate-pulse rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="h-4 w-28 rounded bg-slate-100" />
                <div className="mt-4 h-6 w-52 rounded bg-slate-100" />
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
          <p className="text-lg font-semibold text-slate-900">Unable to load notifications</p>
          <p className="mt-2 text-sm text-slate-600">{error}</p>
          <button
            type="button"
            onClick={loadNotifications}
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
        <section className="rounded-3xl bg-gradient-to-r from-violet-600 to-purple-500 p-6 text-white shadow-lg shadow-violet-100">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-violet-100">
            Notifications
          </p>
          <h1 className="mt-2 text-3xl font-bold">Your farm updates</h1>
          <p className="mt-2 text-sm text-violet-50">
            Keep track of alerts, advisories, and intervention progress.
          </p>
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Unread</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">{unread}</p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Read</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">
              {items.length - unread}
            </p>
          </div>
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-100">
            <p className="text-sm text-slate-500">Total</p>
            <p className="mt-3 text-3xl font-bold text-slate-900">{items.length}</p>
          </div>
        </section>

        {items.length === 0 ? (
          <section className="rounded-3xl border border-dashed border-slate-200 bg-white p-8 text-center shadow-sm">
            <p className="text-xl font-semibold text-slate-900">No notifications yet</p>
            <p className="mt-2 text-sm text-slate-600">
              Alerts and advisory updates will appear here as they are created.
            </p>
          </section>
        ) : (
          <section className="space-y-4">
            {items.map((item) => (
              <article
                key={item.id}
                className={`rounded-3xl p-5 shadow-sm ring-1 ${
                  item.status === "UNREAD"
                    ? "border border-violet-200 bg-violet-50 ring-violet-100"
                    : "bg-white ring-slate-100"
                }`}
              >
                <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="rounded-full bg-slate-200 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-slate-700">
                        {item.type || "Update"}
                      </span>
                      {item.status === "UNREAD" && (
                        <span className="rounded-full bg-violet-100 px-2 py-1 text-[10px] font-semibold uppercase tracking-wide text-violet-700">
                          New
                        </span>
                      )}
                    </div>
                    <h2 className="mt-3 text-xl font-bold text-slate-900">
                      {item.title || "Farm update"}
                    </h2>
                  </div>

                  {item.status === "UNREAD" && (
                    <button
                      type="button"
                      onClick={() => markRead(item.id)}
                      className="rounded-xl bg-violet-600 px-3 py-2 text-sm font-medium text-white hover:bg-violet-700"
                    >
                      Mark read
                    </button>
                  )}
                </div>

                <p className="mt-3 text-sm leading-7 text-slate-700">{item.message}</p>

                <div className="mt-4 flex flex-wrap items-center gap-3 text-xs text-slate-500">
                  <span>{item.cropName || "Farm alert"}</span>
                  <span>•</span>
                  <span>{formatDate(item.createdAt)}</span>
                  {item.readAt && (
                    <>
                      <span>•</span>
                      <span>Read {formatDate(item.readAt)}</span>
                    </>
                  )}
                </div>
              </article>
            ))}
          </section>
        )}
      </div>
    </div>
  );
};

export default Notifications;
