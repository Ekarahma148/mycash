document.addEventListener("DOMContentLoaded", function () {
  const options = {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  };

  const today = new Date().toLocaleDateString("id-ID", options);
  const dateElement = document.getElementById("current-date");

  if (dateElement) {
    dateElement.textContent = today;
  }

  initResponsiveSidebar();
});

function initResponsiveSidebar() {
  const sidebar = document.querySelector(".sidebar");

  if (!sidebar) {
    return;
  }

  /*
   * Hindari membuat hamburger lebih dari sekali
   */
  if (document.querySelector(".mobile-menu-button")) {
    return;
  }

  /* =========================
       SIDEBAR ID
    ========================= */

  sidebar.id = "mycash-sidebar";

  /* =========================
       HAMBURGER
    ========================= */

  const button = document.createElement("button");

  button.type = "button";
  button.className = "mobile-menu-button";

  button.setAttribute("aria-label", "Buka menu navigasi");

  button.setAttribute("aria-expanded", "false");

  button.setAttribute("aria-controls", "mycash-sidebar");

  button.innerHTML = '<i class="fas fa-bars" aria-hidden="true"></i>';

  /* =========================
       OVERLAY
    ========================= */

  const overlay = document.createElement("div");

  overlay.className = "sidebar-overlay";

  overlay.setAttribute("aria-hidden", "true");

  document.body.appendChild(button);
  document.body.appendChild(overlay);

  /* =========================
       OPEN / CLOSE
    ========================= */

  function setOpen(open) {
    sidebar.classList.toggle("mobile-open", open);

    overlay.classList.toggle("active", open);

    button.classList.toggle("is-open", open);

    button.setAttribute("aria-expanded", String(open));

    button.setAttribute(
      "aria-label",
      open ? "Tutup menu navigasi" : "Buka menu navigasi",
    );

    button.innerHTML = open
      ? '<i class="fas fa-times" aria-hidden="true"></i>'
      : '<i class="fas fa-bars" aria-hidden="true"></i>';

    document.body.classList.toggle("sidebar-menu-open", open);
  }

  /* =========================
       HAMBURGER CLICK
    ========================= */

  button.addEventListener("click", function () {
    const isOpen = sidebar.classList.contains("mobile-open");

    setOpen(!isOpen);
  });

  /* =========================
       OVERLAY CLICK
    ========================= */

  overlay.addEventListener("click", function () {
    setOpen(false);
  });

  /* =========================
       MENU CLICK
    ========================= */

  sidebar.querySelectorAll(".sidebar-nav a").forEach(function (link) {
    link.addEventListener("click", function () {
      if (window.innerWidth <= 768) {
        setOpen(false);
      }
    });
  });

  /* =========================
       ESC KEY
    ========================= */

  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && sidebar.classList.contains("mobile-open")) {
      setOpen(false);
    }
  });

  /* =========================
       RESIZE
    ========================= */

  window.addEventListener("resize", function () {
    if (window.innerWidth > 768) {
      setOpen(false);
    }
  });
}

/*
 * Backward-compatible function
 */
function toggleSidebar() {
  const sidebar = document.querySelector(".sidebar");

  const button = document.querySelector(".mobile-menu-button");

  const overlay = document.querySelector(".sidebar-overlay");

  if (!sidebar) {
    return;
  }

  const open = !sidebar.classList.contains("mobile-open");

  sidebar.classList.toggle("mobile-open", open);

  if (overlay) {
    overlay.classList.toggle("active", open);
  }

  if (button) {
    button.setAttribute("aria-expanded", String(open));

    button.classList.toggle("is-open", open);

    button.innerHTML = open
      ? '<i class="fas fa-times" aria-hidden="true"></i>'
      : '<i class="fas fa-bars" aria-hidden="true"></i>';
  }

  document.body.classList.toggle("sidebar-menu-open", open);
}
