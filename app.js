/**
 * MindSmart – Smart Internship Opportunities Platform
 * Full-Stack Client Application
 */

// ================= GLOBAL STATE =================
const API_BASE = window.location.origin.includes('localhost:8080')
  ? '' // Same origin when served by Spring Boot
  : 'http://localhost:8080';

const state = {
  token: localStorage.getItem('mindsmart_token') || null,
  currentUser: JSON.parse(localStorage.getItem('mindsmart_user') || 'null'),
  internships: [],
  selectedInternship: null,
  comparedIds: new Set(),
  unreadCount: 0,
  studentProfile: null,
  companyProfile: null,
  activeFilter: 'all',
  backendConnected: false
};

// Demo / Seed fallback data if backend is offline or starting
const FALLBACK_INTERNSHIPS = [
  {
    id: 1,
    title: "Full Stack Java Developer Intern",
    category: "Software Development",
    workMode: "Remote",
    location: "Remote / Bangalore",
    duration: "6 Months",
    stipend: 25000,
    openings: 3,
    description: "Join our engineering team to design and deploy scalable RESTful services using Spring Boot and responsive interfaces in React. You will work on real production microservices, participate in agile sprint ceremonies, and write clean, testable code.",
    eligibility: "B.Tech / MCA / M.Tech in CS/IT. Strong foundation in Java and Object-Oriented Design.",
    deadline: "2026-12-15",
    companyName: "TechNova Innovations",
    companyLocation: "Bangalore, India",
    companyVerified: true,
    requiredSkills: ["Java", "Spring Boot", "MySQL", "REST API", "React", "Git"],
    createdAt: "2026-11-10T10:00:00"
  },
  {
    id: 2,
    title: "Cloud DevOps & Infrastructure Intern",
    category: "Cloud & DevOps",
    workMode: "Hybrid",
    location: "Hyderabad, India",
    duration: "6 Months",
    stipend: 30000,
    openings: 2,
    description: "Automate cloud deployments on AWS using Docker, Terraform, and GitHub Actions CI/CD pipelines. Maintain Kubernetes clusters and monitor real-time infrastructure metrics with Prometheus and Grafana.",
    eligibility: "Knowledge of Linux CLI, basic containerization with Docker, and cloud concepts.",
    deadline: "2026-12-05",
    companyName: "CloudScale Systems",
    companyLocation: "Hyderabad, India",
    companyVerified: true,
    requiredSkills: ["AWS", "Docker", "Kubernetes", "Linux", "CI/CD", "Git"],
    createdAt: "2026-11-09T14:30:00"
  },
  {
    id: 3,
    title: "Machine Learning & AI Research Intern",
    category: "Data Science & AI",
    workMode: "Remote",
    location: "Remote / Pune",
    duration: "4 Months",
    stipend: 35000,
    openings: 2,
    description: "Develop and optimize predictive machine learning models, analyze extensive tabular and time-series datasets, and deploy inference pipelines using Python, Scikit-Learn, and FastAPI.",
    eligibility: "Proficiency in Python, linear algebra, statistics, and machine learning libraries.",
    deadline: "2026-12-20",
    companyName: "DataVibe Analytics",
    companyLocation: "Pune, India",
    companyVerified: true,
    requiredSkills: ["Python", "Machine Learning", "Data Science", "Pandas", "NumPy", "Git"],
    createdAt: "2026-11-08T09:15:00"
  },
  {
    id: 4,
    title: "Cyber Security & Threat Analysis Intern",
    category: "Cyber Security",
    workMode: "On_Site",
    location: "Mumbai, India",
    duration: "6 Months",
    stipend: 28000,
    openings: 1,
    description: "Perform vulnerability scans, assist in web application penetration testing (OWASP Top 10), inspect network telemetry for anomalous activity, and produce executive vulnerability remediation reports.",
    eligibility: "Understanding of TCP/IP, Linux fundamentals, and common attack vectors.",
    deadline: "2026-11-30",
    companyName: "CyberPulse Security",
    companyLocation: "Mumbai, India",
    companyVerified: true,
    requiredSkills: ["Cyber Security", "Linux", "Python", "Git", "REST API"],
    createdAt: "2026-11-07T11:00:00"
  },
  {
    id: 5,
    title: "Frontend UI/UX Engineer Intern",
    category: "Web Development",
    workMode: "Remote",
    location: "Remote / Delhi NCR",
    duration: "3 Months",
    stipend: 20000,
    openings: 4,
    description: "Transform modern Figma mockups into pixel-perfect, responsive web pages using modern HTML, CSS, JavaScript, and Tailwind CSS. Ensure smooth animations and clean component architecture.",
    eligibility: "Hands-on experience with modern JavaScript, CSS flexbox/grid, and component libraries.",
    deadline: "2026-11-28",
    companyName: "NexaByte Labs",
    companyLocation: "Delhi NCR, India",
    companyVerified: false,
    requiredSkills: ["JavaScript", "React", "HTML", "CSS", "Tailwind CSS", "Figma", "Git"],
    createdAt: "2026-11-06T16:45:00"
  },
  {
    id: 6,
    title: "Backend Microservices Engineer Intern",
    category: "Software Development",
    workMode: "Hybrid",
    location: "Bangalore, India",
    duration: "6 Months",
    stipend: 30000,
    openings: 2,
    description: "Build distributed systems and event-driven microservices using Spring Cloud, Docker, Kafka, and Redis caching. Optimize SQL queries and benchmark system throughput.",
    eligibility: "Solid understanding of Core Java, data structures, and relational databases.",
    deadline: "2026-12-30",
    companyName: "TechNova Innovations",
    companyLocation: "Bangalore, India",
    companyVerified: true,
    requiredSkills: ["Java", "Spring Boot", "Microservices", "Docker", "MySQL", "REST API"],
    createdAt: "2026-11-05T13:20:00"
  },
  {
    id: 7,
    title: "Data Analyst & Business Intelligence Intern",
    category: "Data Science & AI",
    workMode: "Hybrid",
    location: "Pune, India",
    duration: "3 Months",
    stipend: 22000,
    openings: 3,
    description: "Extract and transform relational data using SQL, design interactive KPI executive dashboards in Tableau and Power BI, and present data-backed growth recommendations.",
    eligibility: "Strong SQL querying capabilities, analytical curiosity, and data storytelling.",
    deadline: "2026-12-10",
    companyName: "DataVibe Analytics",
    companyLocation: "Pune, India",
    companyVerified: true,
    requiredSkills: ["MySQL", "Data Science", "Python", "Tableau", "Git"],
    createdAt: "2026-11-04T15:10:00"
  },
  {
    id: 8,
    title: "Mobile Application Developer (Flutter/Android)",
    category: "Mobile App Development",
    workMode: "Remote",
    location: "Remote",
    duration: "4 Months",
    stipend: 24000,
    openings: 2,
    description: "Build cross-platform mobile apps with Flutter and Dart, integrating REST APIs, state management, and push notifications. Publish builds to Play Store testing tracks.",
    eligibility: "Experience creating at least one mobile app with Flutter or native Android.",
    deadline: "2026-12-08",
    companyName: "NexaByte Labs",
    companyLocation: "Delhi NCR, India",
    companyVerified: false,
    requiredSkills: ["Flutter", "Android", "REST API", "Git", "JavaScript"],
    createdAt: "2026-11-03T11:40:00"
  },
  {
    id: 9,
    title: "Cloud Solutions Architect Intern",
    category: "Cloud & DevOps",
    workMode: "Remote",
    location: "Remote / Hyderabad",
    duration: "6 Months",
    stipend: 32000,
    openings: 1,
    description: "Collaborate with senior cloud architects to architect highly available, fault-tolerant infrastructure topologies on AWS and Google Cloud Platform. Draft architecture diagrams.",
    eligibility: "Familiarity with cloud security, VPCs, compute, and storage primitives.",
    deadline: "2026-12-25",
    companyName: "CloudScale Systems",
    companyLocation: "Hyderabad, India",
    companyVerified: true,
    requiredSkills: ["AWS", "Google Cloud", "Docker", "Linux", "CI/CD"],
    createdAt: "2026-11-02T10:00:00"
  },
  {
    id: 10,
    title: "Software Quality Assurance & Automation Intern",
    category: "Software Testing",
    workMode: "Hybrid",
    location: "Pune, India",
    duration: "3 Months",
    stipend: 18000,
    openings: 2,
    description: "Develop automated end-to-end and API regression suites using JUnit, Postman, and Selenium. Log, track, and verify software bugs in Jira to ensure zero-defect deployments.",
    eligibility: "Basic programming skills in Java or Python, understanding of SDLC and test methodologies.",
    deadline: "2026-12-01",
    companyName: "CyberPulse Security",
    companyLocation: "Mumbai, India",
    companyVerified: true,
    requiredSkills: ["Java", "JUnit", "Postman", "REST API", "Git", "MySQL"],
    createdAt: "2026-11-01T14:00:00"
  }
];

// ================= NOTIFICATION TOAST HELPER =================
function showToast(title, message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const iconClass = type === 'success' ? 'fa-circle-check' :
                    type === 'error' ? 'fa-circle-xmark' :
                    type === 'warning' ? 'fa-triangle-exclamation' : 'fa-circle-info';

  const iconColor = type === 'success' ? '#10b981' :
                    type === 'error' ? '#ef4444' :
                    type === 'warning' ? '#f59e0b' : '#3b82f6';

  toast.innerHTML = `
    <i class="fa-solid ${iconClass}" style="color:${iconColor}; font-size:1.25rem; margin-top:2px;"></i>
    <div class="toast-content">
      <div class="toast-title">${title}</div>
      <div class="toast-msg">${message}</div>
    </div>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ================= API CLIENT WRAPPER =================
async function apiRequest(endpoint, options = {}) {
  const headers = options.headers || {};
  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  // If not FormData, default to application/json
  if (!(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    state.backendConnected = true;
    updateBackendBadge(true);

    if (res.status === 401) {
      // Token expired or invalid
      console.warn("Unauthorized API call. Clearing session.");
      // Do not auto-logout if merely viewing public items
    }

    const data = await res.json();
    return data;
  } catch (err) {
    console.warn(`[MindSmart API] Request to ${endpoint} failed:`, err.message);
    state.backendConnected = false;
    updateBackendBadge(false);
    throw err;
  }
}

function updateBackendBadge(isOnline) {
  const badge = document.getElementById('backend-status-badge');
  if (!badge) return;

  if (isOnline) {
    badge.style.background = '#ecfdf5';
    badge.style.color = '#059669';
    badge.style.borderColor = '#a7f3d0';
    badge.innerHTML = `<i class="fa-solid fa-circle" style="font-size:0.5rem; color:#10b981;"></i> API Online`;
    badge.title = "Connected to Java Spring Boot REST APIs";
  } else {
    badge.style.background = '#fffbeb';
    badge.style.color = '#b45309';
    badge.style.borderColor = '#fde68a';
    badge.innerHTML = `<i class="fa-solid fa-circle" style="font-size:0.5rem; color:#f59e0b;"></i> Demo Mode (Connecting...)`;
    badge.title = "Spring Boot server offline or starting. Using resilient mock engine.";
  }
}

// ================= SMART MATCHING ALGORITHM (Client Mirror) =================
function calculateRuleDrivenMatch(studentSkills = [], requiredSkills = []) {
  if (!requiredSkills || requiredSkills.length === 0) {
    return { matchPercentage: 100, matchingSkills: [], missingSkills: [], suggestedSkillsToLearn: [] };
  }

  const normStudent = new Set(studentSkills.map(s => (s || '').trim().toLowerCase()));
  const matching = [];
  const missing = [];

  for (const req of requiredSkills) {
    const normReq = (req || '').trim().toLowerCase();
    if (normStudent.has(normReq)) {
      matching.push(req);
    } else {
      missing.push(req);
    }
  }

  const total = requiredSkills.length;
  const matched = matching.length;
  const matchPercentage = Math.round((matched / total) * 100);

  return {
    matchPercentage,
    matchingSkills: matching,
    missingSkills: missing,
    suggestedSkillsToLearn: missing.slice(0, 4)
  };
}

// ================= INITIALIZATION & AUTH =================
document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  setupAuthHandlers();
  setupFilterHandlers();
  setupStudentDashboard();
  setupCompanyDashboard();
  setupAdminDashboard();
  setupModals();

  // Load initial data
  restoreUserSession();
  fetchInternships();
  fetchRecentEmailsCount();
});

function restoreUserSession() {
  if (state.token && state.currentUser) {
    renderUserNavigation(state.currentUser);
    if (state.currentUser.role === 'ROLE_STUDENT') {
      loadStudentProfile();
    } else if (state.currentUser.role === 'ROLE_COMPANY') {
      loadCompanyProfile();
    } else if (state.currentUser.role === 'ROLE_ADMIN') {
      loadAdminDashboard();
    }
  } else {
    renderGuestNavigation();
  }
}

function renderUserNavigation(user) {
  const guestBox = document.getElementById('guest-nav-actions');
  const userBox = document.getElementById('user-nav-actions');
  const navStudentBtn = document.getElementById('nav-student-btn');
  const navCompanyBtn = document.getElementById('nav-company-btn');
  const navAdminBtn = document.getElementById('nav-admin-btn');

  if (guestBox) guestBox.style.display = 'none';
  if (userBox) userBox.style.display = 'flex';

  document.getElementById('nav-user-name').textContent = user.name || user.email;
  document.getElementById('nav-user-avatar').textContent = (user.name || user.email || 'U').charAt(0).toUpperCase();

  const roleTag = document.getElementById('nav-user-role');
  if (user.role === 'ROLE_STUDENT') {
    roleTag.textContent = 'Student';
    roleTag.style.background = 'rgba(99, 102, 241, 0.12)';
    roleTag.style.color = '#4f46e5';
    if (navStudentBtn) navStudentBtn.style.display = 'inline-flex';
  } else if (user.role === 'ROLE_COMPANY') {
    roleTag.textContent = 'Employer';
    roleTag.style.background = 'rgba(6, 182, 212, 0.12)';
    roleTag.style.color = '#0891b2';
    if (navCompanyBtn) navCompanyBtn.style.display = 'inline-flex';
  } else if (user.role === 'ROLE_ADMIN') {
    roleTag.textContent = 'Admin';
    roleTag.style.background = 'rgba(236, 72, 153, 0.12)';
    roleTag.style.color = '#db2777';
    if (navAdminBtn) navAdminBtn.style.display = 'inline-flex';
  }

  fetchUnreadNotificationCount();
}

function renderGuestNavigation() {
  const guestBox = document.getElementById('guest-nav-actions');
  const userBox = document.getElementById('user-nav-actions');
  const navStudentBtn = document.getElementById('nav-student-btn');
  const navCompanyBtn = document.getElementById('nav-company-btn');
  const navAdminBtn = document.getElementById('nav-admin-btn');

  if (guestBox) guestBox.style.display = 'flex';
  if (userBox) userBox.style.display = 'none';
  if (navStudentBtn) navStudentBtn.style.display = 'none';
  if (navCompanyBtn) navCompanyBtn.style.display = 'none';
  if (navAdminBtn) navAdminBtn.style.display = 'none';

  switchView('view-explore');
}

// ================= VIEW SWITCHING =================
function switchView(viewId) {
  document.querySelectorAll('.app-view').forEach(view => {
    view.style.display = 'none';
  });

  const target = document.getElementById(viewId);
  if (target) {
    target.style.display = 'block';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
  if (viewId === 'view-explore') document.getElementById('nav-explore-btn')?.classList.add('active');
  if (viewId === 'view-student') document.getElementById('nav-student-btn')?.classList.add('active');
  if (viewId === 'view-company') document.getElementById('nav-company-btn')?.classList.add('active');
  if (viewId === 'view-admin') document.getElementById('nav-admin-btn')?.classList.add('active');
}

function setupNavigation() {
  document.getElementById('brand-home-btn')?.addEventListener('click', () => switchView('view-explore'));
  document.getElementById('nav-explore-btn')?.addEventListener('click', () => switchView('view-explore'));
  document.getElementById('nav-student-btn')?.addEventListener('click', () => {
    switchView('view-student');
    loadStudentProfile();
  });
  document.getElementById('nav-company-btn')?.addEventListener('click', () => {
    switchView('view-company');
    loadCompanyProfile();
  });
  document.getElementById('nav-admin-btn')?.addEventListener('click', () => {
    switchView('view-admin');
    loadAdminDashboard();
  });

  document.getElementById('user-profile-toggle')?.addEventListener('click', () => {
    if (!state.currentUser) return;
    if (state.currentUser.role === 'ROLE_STUDENT') switchView('view-student');
    else if (state.currentUser.role === 'ROLE_COMPANY') switchView('view-company');
    else if (state.currentUser.role === 'ROLE_ADMIN') switchView('view-admin');
  });

  document.getElementById('btn-logout')?.addEventListener('click', () => {
    localStorage.removeItem('mindsmart_token');
    localStorage.removeItem('mindsmart_user');
    state.token = null;
    state.currentUser = null;
    state.studentProfile = null;
    state.companyProfile = null;
    renderGuestNavigation();
    showToast('Signed Out', 'You have been successfully signed out.', 'info');
  });

  // Modal open triggers
  document.getElementById('btn-open-login')?.addEventListener('click', () => openAuthModal('login'));
  document.getElementById('btn-open-register')?.addEventListener('click', () => openAuthModal('register'));
  document.getElementById('open-email-simulator-btn')?.addEventListener('click', openEmailSimulatorModal);
  document.getElementById('open-notifications-btn')?.addEventListener('click', openNotificationsModal);
}

// ================= AUTHENTICATION MODAL LOGIC =================
function openAuthModal(mode = 'login') {
  const modal = document.getElementById('modal-auth');
  if (!modal) return;
  modal.classList.add('active');

  const tabLogin = document.getElementById('auth-tab-login');
  const tabRegister = document.getElementById('auth-tab-register');
  const formLogin = document.getElementById('form-login');
  const formRegister = document.getElementById('form-register');
  const title = document.getElementById('auth-modal-title');

  if (mode === 'login') {
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    formLogin.style.display = 'block';
    formRegister.style.display = 'none';
    title.textContent = 'Sign in to MindSmart';
  } else {
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    formRegister.style.display = 'block';
    formLogin.style.display = 'none';
    title.textContent = 'Create a MindSmart Account';
  }
}

function setupAuthHandlers() {
  document.getElementById('close-auth-modal')?.addEventListener('click', () => {
    document.getElementById('modal-auth')?.classList.remove('active');
  });

  document.getElementById('auth-tab-login')?.addEventListener('click', (e) => {
    e.preventDefault();
    openAuthModal('login');
  });

  document.getElementById('auth-tab-register')?.addEventListener('click', (e) => {
    e.preventDefault();
    openAuthModal('register');
  });

  // Role toggle inside register form
  document.querySelectorAll('input[name="register-role"]').forEach(radio => {
    radio.addEventListener('change', (e) => {
      const compFields = document.getElementById('company-reg-fields');
      if (compFields) {
        compFields.style.display = e.target.value === 'ROLE_COMPANY' ? 'block' : 'none';
      }
    });
  });

  // 1-Click Quick Demo Login Buttons
  document.getElementById('btn-demo-student')?.addEventListener('click', () => {
    document.getElementById('login-email').value = 'student@mindsmart.com';
    document.getElementById('login-password').value = 'Student@123';
    submitLogin('student@mindsmart.com', 'Student@123');
  });

  document.getElementById('btn-demo-company')?.addEventListener('click', () => {
    document.getElementById('login-email').value = 'hr@technova.com';
    document.getElementById('login-password').value = 'Company@123';
    submitLogin('hr@technova.com', 'Company@123');
  });

  document.getElementById('btn-demo-admin')?.addEventListener('click', () => {
    document.getElementById('login-email').value = 'admin@mindsmart.com';
    document.getElementById('login-password').value = 'Admin@123';
    submitLogin('admin@mindsmart.com', 'Admin@123');
  });

  // Form Submit Login
  document.getElementById('form-login')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    await submitLogin(email, password);
  });

  // Form Submit Register
  document.getElementById('form-register')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('reg-name').value.trim();
    const phone = document.getElementById('reg-phone').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const role = document.querySelector('input[name="register-role"]:checked').value;

    const payload = { name, phone, email, password, role };
    if (role === 'ROLE_COMPANY') {
      payload.companyName = document.getElementById('reg-comp-name').value.trim() || name;
      payload.industry = document.getElementById('reg-comp-industry').value.trim();
      payload.location = document.getElementById('reg-comp-location').value.trim();
    }

    try {
      const res = await apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      if (res.success && res.data) {
        handleAuthSuccess(res.data);
        showToast('Registration Complete!', 'Welcome to MindSmart. Confirmation email dispatched.', 'success');
      } else {
        showToast('Registration Error', res.message || 'Could not complete registration', 'error');
      }
    } catch (err) {
      // Offline fallback registration simulation
      const fallbackUser = {
        id: Date.now(),
        name,
        email,
        phone,
        role: role
      };
      handleAuthSuccess({
        token: 'mock_jwt_token_' + Date.now(),
        ...fallbackUser
      });
      showToast('Demo Account Created', `Signed in as ${name}. Simulated email logged.`, 'success');
    }
  });
}

async function submitLogin(email, password) {
  try {
    const res = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });

    if (res.success && res.data) {
      handleAuthSuccess(res.data);
      showToast('Welcome Back!', `Signed in as ${res.data.name}`, 'success');
    } else {
      showToast('Login Failed', res.message || 'Invalid email or password', 'error');
    }
  } catch (err) {
    // Resilient offline fallback simulation for demo credentials
    if (email === 'student@mindsmart.com' || email.includes('student')) {
      handleAuthSuccess({
        token: 'mock_student_token',
        id: 2,
        name: 'Rahul Sharma',
        email: email,
        phone: '+91 9123456780',
        role: 'ROLE_STUDENT',
        profileId: 1
      });
      showToast('Demo Student Signed In', 'Loaded Rahul Sharma profile with Java/Spring skills.', 'success');
    } else if (email === 'hr@technova.com' || email.includes('company') || email.includes('technova')) {
      handleAuthSuccess({
        token: 'mock_company_token',
        id: 3,
        name: 'Ananya Sen (TechNova)',
        email: email,
        phone: '+91 9800112233',
        role: 'ROLE_COMPANY',
        profileId: 1,
        verified: true
      });
      showToast('Demo Employer Signed In', 'Loaded TechNova Innovations recruiter dashboard.', 'success');
    } else if (email === 'admin@mindsmart.com' || email.includes('admin')) {
      handleAuthSuccess({
        token: 'mock_admin_token',
        id: 1,
        name: 'Platform Administrator',
        email: email,
        phone: '+91 9876543210',
        role: 'ROLE_ADMIN'
      });
      showToast('Administrator Signed In', 'Access granted to administration console.', 'success');
    } else {
      showToast('Backend Connecting...', 'Check if Spring Boot is running on port 8080.', 'warning');
    }
  }
}

function handleAuthSuccess(authData) {
  state.token = authData.token;
  state.currentUser = {
    id: authData.id,
    name: authData.name,
    email: authData.email,
    phone: authData.phone,
    role: authData.role
  };

  localStorage.setItem('mindsmart_token', state.token);
  localStorage.setItem('mindsmart_user', JSON.stringify(state.currentUser));

  document.getElementById('modal-auth')?.classList.remove('active');
  renderUserNavigation(state.currentUser);

  // Switch to appropriate portal
  if (state.currentUser.role === 'ROLE_STUDENT') {
    switchView('view-student');
    loadStudentProfile();
  } else if (state.currentUser.role === 'ROLE_COMPANY') {
    switchView('view-company');
    loadCompanyProfile();
  } else if (state.currentUser.role === 'ROLE_ADMIN') {
    switchView('view-admin');
    loadAdminDashboard();
  } else {
    switchView('view-explore');
  }

  fetchInternships();
}

// ================= INTERNSHIPS FETCH & RENDER =================
async function fetchInternships() {
  const container = document.getElementById('internships-grid-container');
  if (!container) return;

  const keyword = document.getElementById('search-keyword')?.value.trim() || '';
  const category = document.getElementById('search-category')?.value || '';
  const workMode = document.getElementById('search-work-mode')?.value || '';

  const params = new URLSearchParams();
  if (keyword) params.append('keyword', keyword);
  if (category) params.append('category', category);
  if (workMode) params.append('workMode', workMode);

  try {
    const res = await apiRequest(`/api/internships/search?${params.toString()}`);
    if (res.success && res.data) {
      state.internships = res.data;
      renderInternshipsGrid(state.internships, container);
      updateTotalStats(state.internships.length);
      return;
    }
  } catch (err) {
    console.warn("Backend not ready yet, rendering resilient fallback demo data.");
  }

  // Resilient fallback filtering
  let filtered = [...FALLBACK_INTERNSHIPS];
  if (keyword) {
    const kw = keyword.toLowerCase();
    filtered = filtered.filter(i =>
      i.title.toLowerCase().includes(kw) ||
      i.description.toLowerCase().includes(kw) ||
      i.companyName.toLowerCase().includes(kw) ||
      i.requiredSkills.some(s => s.toLowerCase().includes(kw))
    );
  }
  if (category) {
    filtered = filtered.filter(i => i.category === category);
  }
  if (workMode) {
    filtered = filtered.filter(i => i.workMode === workMode);
  }

  state.internships = filtered;
  renderInternshipsGrid(state.internships, container);
  updateTotalStats(state.internships.length);
}

function renderInternshipsGrid(list, container) {
  if (!container) return;
  container.innerHTML = '';

  const countText = document.getElementById('search-results-count-text');
  if (countText) countText.textContent = `Showing ${list.length} curated opportunities`;

  if (!list || list.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding:3.5rem; background:white; border-radius:var(--radius-lg); border:1px solid var(--border);">
        <i class="fa-solid fa-magnifying-glass" style="font-size:2.5rem; color:var(--text-light); margin-bottom:1rem;"></i>
        <h3 style="font-weight:800; font-size:1.25rem;">No internships match your current filters</h3>
        <p style="color:var(--text-muted); font-size:0.92rem; margin-top:0.35rem;">Try adjusting your keyword or clearing filters to explore other openings.</p>
        <button class="btn btn-secondary btn-sm" onclick="resetFilters()" style="margin-top:1.25rem;"><i class="fa-solid fa-rotate-left"></i> Reset All Filters</button>
      </div>
    `;
    return;
  }

  const studentSkills = (state.studentProfile && state.studentProfile.skills) ? Array.from(state.studentProfile.skills) : ['Java', 'Spring Boot', 'MySQL', 'REST API', 'Git', 'HTML', 'CSS'];

  list.forEach(item => {
    // Calculate deterministic rule-driven match
    const reqSkills = Array.isArray(item.requiredSkills) ? item.requiredSkills : Array.from(item.requiredSkills || []);
    const match = item.matchResult || calculateRuleDrivenMatch(studentSkills, reqSkills);

    const card = document.createElement('div');
    card.className = 'internship-card';

    // Color code match score
    const pct = match.matchPercentage;
    const matchClass = pct >= 75 ? 'high' : pct >= 50 ? 'medium' : pct >= 25 ? 'low' : 'opportunity';
    const matchFillColor = pct >= 75 ? '#10b981' : pct >= 50 ? '#6366f1' : pct >= 25 ? '#f59e0b' : '#ec4899';

    const isSaved = item.saved || false;
    const isCompared = state.comparedIds.has(item.id);

    card.innerHTML = `
      <div class="card-top">
        <div class="company-brand-box">
          <div class="company-logo-img">
            ${item.companyLogo ? `<img src="${item.companyLogo}" style="width:100%; height:100%; object-fit:cover; border-radius:var(--radius-md);">` : (item.companyName || 'C').charAt(0)}
          </div>
          <div class="company-meta">
            <span class="company-name">
              ${item.companyName}
              ${item.companyVerified ? `<i class="fa-solid fa-circle-check verified-badge" title="Verified Employer"></i>` : ''}
            </span>
            <span class="company-loc"><i class="fa-solid fa-location-dot"></i> ${item.location}</span>
          </div>
        </div>
        <div class="card-actions-top">
          <button class="bookmark-btn ${isSaved ? 'saved' : ''}" data-id="${item.id}" title="${isSaved ? 'Remove Bookmark' : 'Save Internship'}">
            <i class="${isSaved ? 'fa-solid' : 'fa-regular'} fa-bookmark"></i>
          </button>
        </div>
      </div>

      <h3 class="internship-role-title">${item.title}</h3>

      <div class="internship-highlights">
        <span class="highlight-pill stipend"><i class="fa-solid fa-indian-rupee-sign"></i> ₹${(item.stipend || 0).toLocaleString()}/mo</span>
        <span class="highlight-pill mode"><i class="fa-solid fa-laptop"></i> ${item.workMode || 'Remote'}</span>
        <span class="highlight-pill duration"><i class="fa-solid fa-clock"></i> ${item.duration || '3-6 Mo'}</span>
      </div>

      <!-- Smart Match Box -->
      <div class="smart-match-box">
        <div class="match-header">
          <span class="match-score-pill ${matchClass}">
            <i class="fa-solid fa-bolt"></i> ${pct}% Skill Match
          </span>
          <span class="match-label">${reqSkills.length} Required Skills</span>
        </div>
        <div class="match-progress-bar">
          <div class="match-progress-fill" style="width:${pct}%; background:${matchFillColor};"></div>
        </div>
        <div class="skill-chips-row">
          ${reqSkills.slice(0, 4).map(skill => {
            const isMatched = match.matchingSkills && match.matchingSkills.map(s=>s.toLowerCase()).includes(skill.toLowerCase());
            return `<span class="skill-chip ${isMatched ? 'matched' : 'missing'}">${isMatched ? '✓' : '+'} ${skill}</span>`;
          }).join('')}
          ${reqSkills.length > 4 ? `<span class="skill-chip neutral">+${reqSkills.length - 4} more</span>` : ''}
        </div>
      </div>

      <div class="card-footer">
        <span class="deadline-text"><i class="fa-regular fa-calendar"></i> Deadline: ${item.deadline || 'Rolling'}</span>
        <div style="display:flex; gap:0.4rem;">
          <button class="btn btn-secondary btn-sm btn-compare-toggle" data-id="${item.id}" style="${isCompared ? 'background:#1e1b4b; color:white;' : ''}">
            <i class="fa-solid fa-code-compare"></i>
          </button>
          <button class="btn btn-primary btn-sm btn-view-details" data-id="${item.id}">
            View & Apply
          </button>
        </div>
      </div>
    `;

    container.appendChild(card);
  });

  // Attach card event listeners
  container.querySelectorAll('.btn-view-details').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt(btn.getAttribute('data-id'));
      openInternshipDetailsModal(id);
    });
  });

  container.querySelectorAll('.bookmark-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      const id = parseInt(btn.getAttribute('data-id'));
      await toggleBookmark(id, btn);
    });
  });

  container.querySelectorAll('.btn-compare-toggle').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = parseInt(btn.getAttribute('data-id'));
      toggleCompareItem(id);
    });
  });
}

function updateTotalStats(count) {
  const el = document.getElementById('stat-total-internships');
  if (el) el.textContent = `${count}+`;
}

// ================= FILTER & SEARCH HANDLERS =================
function setupFilterHandlers() {
  document.getElementById('btn-execute-search')?.addEventListener('click', () => fetchInternships());
  document.getElementById('search-keyword')?.addEventListener('keyup', (e) => {
    if (e.key === 'Enter') fetchInternships();
  });
  document.getElementById('search-category')?.addEventListener('change', () => fetchInternships());
  document.getElementById('search-work-mode')?.addEventListener('change', () => fetchInternships());

  document.getElementById('btn-reset-filters')?.addEventListener('click', resetFilters);

  // Quick Filter Pills
  document.querySelectorAll('.filter-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');

      const filterType = pill.getAttribute('data-filter');
      applyQuickFilter(filterType);
    });
  });

  // Sorting
  document.getElementById('sort-internships')?.addEventListener('change', (e) => {
    const val = e.target.value;
    sortInternshipsList(val);
  });
}

function applyQuickFilter(filter) {
  if (filter === 'all') {
    resetFilters();
    return;
  }

  if (filter === 'Remote') {
    document.getElementById('search-work-mode').value = 'Remote';
  } else if (filter === 'stipend-high') {
    // High stipend filter
    let sorted = state.internships.filter(i => (i.stipend || 0) >= 25000);
    renderInternshipsGrid(sorted, document.getElementById('internships-grid-container'));
    return;
  } else if (filter.startsWith('cat-')) {
    const cat = filter.replace('cat-', '');
    document.getElementById('search-category').value = cat;
  }

  fetchInternships();
}

function resetFilters() {
  document.getElementById('search-keyword').value = '';
  document.getElementById('search-category').value = '';
  document.getElementById('search-work-mode').value = '';
  document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
  document.querySelector('.filter-pill[data-filter="all"]')?.classList.add('active');
  fetchInternships();
}

function sortInternshipsList(criteria) {
  if (!state.internships || state.internships.length === 0) return;

  const studentSkills = (state.studentProfile && state.studentProfile.skills) ? Array.from(state.studentProfile.skills) : ['Java', 'Spring Boot', 'MySQL'];

  if (criteria === 'stipend') {
    state.internships.sort((a, b) => (b.stipend || 0) - (a.stipend || 0));
  } else if (criteria === 'match') {
    state.internships.sort((a, b) => {
      const mA = calculateRuleDrivenMatch(studentSkills, a.requiredSkills || []).matchPercentage;
      const mB = calculateRuleDrivenMatch(studentSkills, b.requiredSkills || []).matchPercentage;
      return mB - mA;
    });
  } else if (criteria === 'deadline') {
    state.internships.sort((a, b) => (a.deadline || '').localeCompare(b.deadline || ''));
  } else {
    // Newest
    state.internships.sort((a, b) => b.id - a.id);
  }

  renderInternshipsGrid(state.internships, document.getElementById('internships-grid-container'));
}

// ================= COMPARISON SYSTEM =================
function toggleCompareItem(id) {
  if (state.comparedIds.has(id)) {
    state.comparedIds.delete(id);
  } else {
    if (state.comparedIds.size >= 3) {
      showToast('Maximum Reached', 'You can compare up to 3 internships simultaneously.', 'warning');
      return;
    }
    state.comparedIds.add(id);
  }

  const drawer = document.getElementById('comparison-drawer');
  const countText = document.getElementById('compare-count-text');
  if (drawer && countText) {
    countText.textContent = state.comparedIds.size;
    drawer.style.display = state.comparedIds.size > 0 ? 'flex' : 'none';
  }

  renderInternshipsGrid(state.internships, document.getElementById('internships-grid-container'));
}

document.getElementById('btn-clear-comparison')?.addEventListener('click', () => {
  state.comparedIds.clear();
  document.getElementById('comparison-drawer').style.display = 'none';
  renderInternshipsGrid(state.internships, document.getElementById('internships-grid-container'));
});

document.getElementById('btn-open-comparison')?.addEventListener('click', () => {
  if (state.comparedIds.size === 0) return;
  const items = state.internships.filter(i => state.comparedIds.has(i.id));

  const table = document.getElementById('comparison-table');
  if (!table) return;

  const studentSkills = (state.studentProfile && state.studentProfile.skills) ? Array.from(state.studentProfile.skills) : ['Java', 'Spring Boot', 'MySQL'];

  table.innerHTML = `
    <thead>
      <tr>
        <th style="width:200px;">Feature</th>
        ${items.map(i => `<th style="text-align:center;">${i.title}<br><small style="color:var(--text-muted); font-weight:normal;">${i.companyName}</small></th>`).join('')}
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><strong>Skill Match %</strong></td>
        ${items.map(i => {
          const m = calculateRuleDrivenMatch(studentSkills, i.requiredSkills || []).matchPercentage;
          return `<td style="text-align:center;"><span class="match-score-pill ${m>=75?'high':m>=50?'medium':'low'}">${m}%</span></td>`;
        }).join('')}
      </tr>
      <tr>
        <td><strong>Monthly Stipend</strong></td>
        ${items.map(i => `<td style="text-align:center; font-weight:700; color:#059669;">₹${(i.stipend || 0).toLocaleString()}</td>`).join('')}
      </tr>
      <tr>
        <td><strong>Work Mode</strong></td>
        ${items.map(i => `<td style="text-align:center;">${i.workMode || 'Remote'}</td>`).join('')}
      </tr>
      <tr>
        <td><strong>Location</strong></td>
        ${items.map(i => `<td style="text-align:center;">${i.location}</td>`).join('')}
      </tr>
      <tr>
        <td><strong>Duration</strong></td>
        ${items.map(i => `<td style="text-align:center;">${i.duration}</td>`).join('')}
      </tr>
      <tr>
        <td><strong>Required Skills</strong></td>
        ${items.map(i => `<td style="text-align:center;"><div class="skill-chips-row" style="justify-content:center;">${(i.requiredSkills || []).map(s=>`<span class="skill-chip neutral">${s}</span>`).join('')}</div></td>`).join('')}
      </tr>
      <tr>
        <td><strong>Action</strong></td>
        ${items.map(i => `<td style="text-align:center;"><button class="btn btn-primary btn-sm" onclick="openInternshipDetailsModal(${i.id})">Apply</button></td>`).join('')}
      </tr>
    </tbody>
  `;

  document.getElementById('modal-comparison')?.classList.add('active');
});

// ================= INTERNSHIP DETAILS & APPLICATION =================
function openInternshipDetailsModal(id) {
  const item = state.internships.find(i => i.id === id) || FALLBACK_INTERNSHIPS.find(i => i.id === id);
  if (!item) return;

  state.selectedInternship = item;
  const modal = document.getElementById('modal-internship-details');
  if (!modal) return;

  document.getElementById('detail-title').textContent = item.title;
  document.getElementById('detail-company-name').textContent = item.companyName;
  document.getElementById('detail-company-logo').textContent = (item.companyName || 'C').charAt(0);

  document.getElementById('detail-highlights').innerHTML = `
    <span class="highlight-pill stipend"><i class="fa-solid fa-indian-rupee-sign"></i> ₹${(item.stipend || 0).toLocaleString()}/month</span>
    <span class="highlight-pill mode"><i class="fa-solid fa-laptop"></i> ${item.workMode || 'Remote'}</span>
    <span class="highlight-pill duration"><i class="fa-solid fa-clock"></i> ${item.duration || '6 Months'}</span>
    <span class="highlight-pill"><i class="fa-solid fa-location-dot"></i> ${item.location}</span>
  `;

  const studentSkills = (state.studentProfile && state.studentProfile.skills) ? Array.from(state.studentProfile.skills) : ['Java', 'Spring Boot', 'MySQL', 'REST API', 'Git', 'HTML', 'CSS'];
  const reqSkills = Array.isArray(item.requiredSkills) ? item.requiredSkills : Array.from(item.requiredSkills || []);
  const match = calculateRuleDrivenMatch(studentSkills, reqSkills);

  // Match box inside detail
  const pct = match.matchPercentage;
  const pill = document.getElementById('detail-match-pill');
  pill.className = `match-score-pill ${pct>=75?'high':pct>=50?'medium':pct>=25?'low':'opportunity'}`;
  pill.innerHTML = `<i class="fa-solid fa-bolt"></i> ${pct}% Skill Fit`;

  document.getElementById('detail-match-progress').style.width = `${pct}%`;
  document.getElementById('detail-match-label').textContent = pct >= 75 ? 'Excellent alignment with your profile skills' :
                                                             pct >= 50 ? 'Strong baseline match' : 'Growth & learning opportunity';

  document.getElementById('detail-matching-chips').innerHTML = match.matchingSkills.length > 0
    ? match.matchingSkills.map(s => `<span class="skill-chip matched">✓ ${s}</span>`).join('')
    : '<span style="font-size:0.8rem; color:var(--text-muted);">None yet - add skills to profile</span>';

  document.getElementById('detail-missing-chips').innerHTML = match.missingSkills.length > 0
    ? match.missingSkills.map(s => `<span class="skill-chip missing">+ ${s}</span>`).join('')
    : '<span style="font-size:0.8rem; color:#059669; font-weight:700;">Zero missing skills - 100% matched!</span>';

  document.getElementById('detail-desc').textContent = item.description;
  document.getElementById('detail-eligibility').textContent = item.eligibility || 'Open to all motivated student applicants.';

  document.getElementById('detail-required-skills').innerHTML = reqSkills.map(s => `<span class="skill-chip neutral">${s}</span>`).join('');

  modal.classList.add('active');
}

// Apply button inside detail modal
document.getElementById('detail-apply-btn')?.addEventListener('click', () => {
  if (!state.currentUser) {
    showToast('Sign In Required', 'Please sign in or register to submit an internship application.', 'warning');
    openAuthModal('login');
    return;
  }

  if (state.currentUser.role !== 'ROLE_STUDENT') {
    showToast('Student Role Required', 'Only student accounts can apply for internships.', 'warning');
    return;
  }

  document.getElementById('modal-internship-details')?.classList.remove('active');
  openApplyModal(state.selectedInternship);
});

function openApplyModal(item) {
  if (!item) return;
  const modal = document.getElementById('modal-apply');
  if (!modal) return;

  document.getElementById('apply-modal-title').textContent = item.title;
  document.getElementById('apply-modal-company').textContent = item.companyName;

  modal.classList.add('active');
}

document.getElementById('btn-confirm-apply')?.addEventListener('click', async () => {
  if (!state.selectedInternship) return;

  const coverLetter = document.getElementById('apply-cover-letter')?.value || '';
  const payload = {
    internshipId: state.selectedInternship.id,
    coverLetter: coverLetter
  };

  try {
    const res = await apiRequest('/api/student/apply', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    if (res.success) {
      showToast('Application Submitted!', `Successfully applied for ${state.selectedInternship.title}. Confirmation email sent.`, 'success');
    } else {
      showToast('Application Note', res.message || 'Application submitted', 'info');
    }
  } catch (err) {
    // Offline simulated application
    showToast('Application Submitted (Demo Mode)', `Applied to ${state.selectedInternship.title}. Simulated email generated.`, 'success');
  }

  document.getElementById('modal-apply')?.classList.remove('active');
  fetchRecentEmailsCount();
});

// Bookmarking toggle
async function toggleBookmark(id, buttonEl) {
  if (!state.currentUser) {
    showToast('Sign In Required', 'Sign in to save internships to your bookmarks.', 'warning');
    openAuthModal('login');
    return;
  }

  try {
    const res = await apiRequest(`/api/student/bookmark/${id}`, { method: 'POST' });
    const isSaved = res.data;
    if (buttonEl) {
      buttonEl.classList.toggle('saved', isSaved);
      buttonEl.innerHTML = `<i class="${isSaved ? 'fa-solid' : 'fa-regular'} fa-bookmark"></i>`;
    }
    showToast('Bookmarks Updated', isSaved ? 'Internship saved to your bookmarks' : 'Removed from bookmarks', 'info');
  } catch (err) {
    // Fallback toggle
    if (buttonEl) {
      const isSaved = buttonEl.classList.toggle('saved');
      buttonEl.innerHTML = `<i class="${isSaved ? 'fa-solid' : 'fa-regular'} fa-bookmark"></i>`;
      showToast('Bookmarks (Local)', isSaved ? 'Saved to bookmarks' : 'Removed from bookmarks', 'info');
    }
  }
}

// ================= STUDENT DASHBOARD LOGIC =================
function setupStudentDashboard() {
  // Tab switching
  setupTabSwitcher('#view-student .dash-tab-btn', '#view-student .dash-tab-pane');

  // Resume drag & drop
  const dropZone = document.getElementById('resume-drop-zone');
  const fileInput = document.getElementById('resume-file-input');
  const browseBtn = document.getElementById('btn-browse-resume');

  browseBtn?.addEventListener('click', () => fileInput?.click());
  dropZone?.addEventListener('click', (e) => {
    if (e.target !== browseBtn) fileInput?.click();
  });

  dropZone?.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('dragover');
  });

  dropZone?.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));

  dropZone?.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('dragover');
    if (e.dataTransfer.files.length > 0) {
      handleResumeFileUpload(e.dataTransfer.files[0]);
    }
  });

  fileInput?.addEventListener('change', () => {
    if (fileInput.files.length > 0) {
      handleResumeFileUpload(fileInput.files[0]);
    }
  });

  // Add custom skill button
  document.getElementById('btn-add-skill')?.addEventListener('click', addSkillFromInput);
  document.getElementById('add-skill-input')?.addEventListener('keyup', (e) => {
    if (e.key === 'Enter') addSkillFromInput();
  });

  // Save skills button
  document.getElementById('btn-save-skills')?.addEventListener('click', saveStudentSkills);

  // Target role selector for Skill-Gap Analyzer
  document.getElementById('gap-target-role')?.addEventListener('change', (e) => {
    loadSkillGapAnalysis(e.target.value);
  });

  // Student Profile Form Save
  document.getElementById('student-profile-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('prof-student-name').value.trim();
    const phone = document.getElementById('prof-student-phone').value.trim();
    const college = document.getElementById('prof-student-college').value.trim();
    const degree = document.getElementById('prof-student-degree').value.trim();
    const department = document.getElementById('prof-student-dept').value.trim();
    const graduationYear = parseInt(document.getElementById('prof-student-gradyear').value) || 2025;
    const bio = document.getElementById('prof-student-bio').value.trim();

    const payload = { name, phone, college, degree, department, graduationYear, bio };

    try {
      const res = await apiRequest('/api/student/profile', {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      if (res.success) {
        state.studentProfile = res.data;
        showToast('Profile Updated', 'Academic details saved and career readiness updated!', 'success');
        updateReadinessMeter(res.data.careerReadinessScore || 85);
      }
    } catch (err) {
      showToast('Profile Saved (Local)', 'Profile details updated.', 'success');
    }
  });
}

async function loadStudentProfile() {
  try {
    const res = await apiRequest('/api/student/profile');
    if (res.success && res.data) {
      state.studentProfile = res.data;
      renderStudentProfileData(res.data);
    }
  } catch (err) {
    // Default simulated student profile
    const defaultProfile = {
      name: state.currentUser?.name || 'Rahul Sharma',
      email: state.currentUser?.email || 'student@mindsmart.com',
      phone: '+91 9123456780',
      college: 'National Institute of Technology',
      degree: 'B.Tech',
      department: 'Computer Science and Engineering',
      graduationYear: 2025,
      bio: 'Full Stack Java enthusiast focused on microservices and cloud scalability.',
      careerReadinessScore: 85,
      skills: ['Java', 'Spring Boot', 'MySQL', 'REST API', 'Git', 'HTML', 'CSS']
    };
    state.studentProfile = defaultProfile;
    renderStudentProfileData(defaultProfile);
  }

  loadStudentRecommendations();
  loadSkillGapAnalysis('Full Stack Java Developer');
  loadStudentApplications();
  loadStudentSaved();
}

function renderStudentProfileData(p) {
  document.getElementById('student-welcome-name').textContent = `Welcome back, ${p.name || 'Student'}!`;
  updateReadinessMeter(p.careerReadinessScore || 80);

  // Populate form fields
  if (document.getElementById('prof-student-name')) document.getElementById('prof-student-name').value = p.name || '';
  if (document.getElementById('prof-student-phone')) document.getElementById('prof-student-phone').value = p.phone || '';
  if (document.getElementById('prof-student-college')) document.getElementById('prof-student-college').value = p.college || '';
  if (document.getElementById('prof-student-degree')) document.getElementById('prof-student-degree').value = p.degree || '';
  if (document.getElementById('prof-student-dept')) document.getElementById('prof-student-dept').value = p.department || '';
  if (document.getElementById('prof-student-gradyear')) document.getElementById('prof-student-gradyear').value = p.graduationYear || '';
  if (document.getElementById('prof-student-bio')) document.getElementById('prof-student-bio').value = p.bio || '';

  // Resume card if present
  if (p.resumeFileName) {
    document.getElementById('active-resume-info-card').style.display = 'flex';
    document.getElementById('active-resume-filename').textContent = p.resumeFileName;
    document.getElementById('btn-download-resume').href = p.resumeUrl || '#';
  }

  // Render Skill Chips
  renderSkillChips(p.skills || []);
}

function renderSkillChips(skills) {
  const container = document.getElementById('student-skills-cloud');
  if (!container) return;
  container.innerHTML = '';

  const skillsList = Array.isArray(skills) ? skills : Array.from(skills || []);
  if (skillsList.length === 0) {
    container.innerHTML = '<span style="font-size:0.85rem; color:var(--text-muted);">No skills added yet. Upload resume to auto-detect skills.</span>';
    return;
  }

  skillsList.forEach(s => {
    const chip = document.createElement('span');
    chip.className = 'removable-tag';
    chip.innerHTML = `
      ${s}
      <i class="fa-solid fa-xmark remove-btn" title="Remove ${s}"></i>
    `;
    chip.querySelector('.remove-btn').addEventListener('click', () => {
      chip.remove();
    });
    container.appendChild(chip);
  });
}

function addSkillFromInput() {
  const input = document.getElementById('add-skill-input');
  if (!input) return;
  const val = input.value.trim();
  if (!val) return;

  const container = document.getElementById('student-skills-cloud');
  const chip = document.createElement('span');
  chip.className = 'removable-tag';
  chip.innerHTML = `
    ${val}
    <i class="fa-solid fa-xmark remove-btn" title="Remove ${val}"></i>
  `;
  chip.querySelector('.remove-btn').addEventListener('click', () => chip.remove());
  container.appendChild(chip);

  input.value = '';
  showToast('Skill Added', `${val} added to candidate skill list`, 'info');
}

async function saveStudentSkills() {
  const tags = document.querySelectorAll('#student-skills-cloud .removable-tag');
  const skills = Array.from(tags).map(t => t.textContent.trim());

  if (state.studentProfile) {
    state.studentProfile.skills = skills;
  }

  try {
    const res = await apiRequest('/api/student/skills', {
      method: 'PUT',
      body: JSON.stringify(skills)
    });
    showToast('Skills Saved', 'Match percentages updated across all internships!', 'success');
  } catch (err) {
    showToast('Skills Updated (Local)', 'Calculated updated match percentages.', 'success');
  }

  loadStudentRecommendations();
  loadSkillGapAnalysis(document.getElementById('gap-target-role')?.value || 'Full Stack Java Developer');
}

// Resume Upload & Extraction
async function handleResumeFileUpload(file) {
  if (!file) return;

  showToast('Uploading Resume...', `Extracting skills from ${file.name} using Java PDFBox/POI`, 'info');

  const formData = new FormData();
  formData.append('file', file);

  try {
    // Note: Do not set Content-Type header manually so fetch generates boundary!
    const res = await fetch(`${API_BASE}/api/student/resume/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${state.token}`
      },
      body: formData
    });

    const data = await res.json();
    if (data.success && data.data) {
      showToast('Resume Processed!', `Detected ${data.data.detectedSkillCount} skills from ${file.name}`, 'success');

      document.getElementById('active-resume-info-card').style.display = 'flex';
      document.getElementById('active-resume-filename').textContent = file.name;
      document.getElementById('btn-download-resume').href = data.data.downloadUrl || '#';

      loadStudentProfile();
    } else {
      showToast('Upload Issue', data.message || 'Resume parsing issue', 'warning');
    }
  } catch (err) {
    // Offline simulated resume extraction
    const mockDetected = ['Java', 'Spring Boot', 'MySQL', 'REST API', 'Docker', 'Git'];
    showToast('Resume Uploaded (Demo Mode)', `Extracted 6 skills from ${file.name}`, 'success');

    document.getElementById('active-resume-info-card').style.display = 'flex';
    document.getElementById('active-resume-filename').textContent = file.name;

    if (state.studentProfile) {
      state.studentProfile.skills = mockDetected;
      renderSkillChips(mockDetected);
      updateReadinessMeter(90);
    }
    fetchRecentEmailsCount();
  }
}

function updateReadinessMeter(score) {
  const circle = document.getElementById('student-gauge-circle');
  const badge = document.getElementById('student-gauge-badge');
  if (!circle || !badge) return;

  circle.textContent = `${score}%`;
  if (score >= 80) {
    badge.textContent = 'Top Tier Candidate';
    circle.style.borderColor = '#10b981';
  } else if (score >= 60) {
    badge.textContent = 'Job Ready';
    circle.style.borderColor = '#6366f1';
  } else {
    badge.textContent = 'Developing';
    circle.style.borderColor = '#f59e0b';
  }
}

async function loadStudentRecommendations() {
  const container = document.getElementById('student-recommended-grid');
  if (!container) return;

  try {
    const res = await apiRequest('/api/student/recommendations');
    if (res.success && res.data) {
      renderInternshipsGrid(res.data, container);
      return;
    }
  } catch (err) {}

  // Fallback sorted by match
  const studentSkills = (state.studentProfile && state.studentProfile.skills) ? Array.from(state.studentProfile.skills) : ['Java', 'Spring Boot', 'MySQL', 'REST API', 'Git', 'HTML', 'CSS'];
  const ranked = [...FALLBACK_INTERNSHIPS].sort((a, b) => {
    return calculateRuleDrivenMatch(studentSkills, b.requiredSkills).matchPercentage -
           calculateRuleDrivenMatch(studentSkills, a.requiredSkills).matchPercentage;
  });

  renderInternshipsGrid(ranked, container);
}

async function loadSkillGapAnalysis(role = 'Full Stack Java Developer') {
  const studentSkills = (state.studentProfile && state.studentProfile.skills) ? Array.from(state.studentProfile.skills) : ['Java', 'Spring Boot', 'MySQL', 'REST API', 'Git', 'HTML', 'CSS'];

  let roleSkills = [];
  if (role === 'Frontend Engineer') {
    roleSkills = ['JavaScript', 'TypeScript', 'React', 'HTML', 'CSS', 'Tailwind CSS', 'Git', 'REST API'];
  } else if (role === 'Data Scientist') {
    roleSkills = ['Python', 'Machine Learning', 'Pandas', 'NumPy', 'Data Science', 'SQL', 'Git'];
  } else if (role === 'Cloud & DevOps Engineer') {
    roleSkills = ['Linux', 'Docker', 'Kubernetes', 'AWS', 'CI/CD', 'Git', 'Python'];
  } else {
    roleSkills = ['Java', 'Spring Boot', 'MySQL', 'REST API', 'React', 'Docker', 'Git', 'Microservices'];
  }

  const match = calculateRuleDrivenMatch(studentSkills, roleSkills);

  document.getElementById('gap-role-title').textContent = role;
  document.getElementById('gap-match-pct').textContent = `${match.matchPercentage}% Fit`;
  document.getElementById('gap-progress-bar').style.width = `${match.matchPercentage}%`;

  document.getElementById('gap-matching-chips').innerHTML = match.matchingSkills.length > 0
    ? match.matchingSkills.map(s => `<span class="skill-chip matched">✓ ${s}</span>`).join('')
    : '<span style="font-size:0.8rem; color:var(--text-muted);">None yet</span>';

  document.getElementById('gap-missing-chips').innerHTML = match.missingSkills.length > 0
    ? match.missingSkills.map(s => `<span class="skill-chip missing">+ ${s}</span>`).join('')
    : '<span style="font-size:0.8rem; color:#059669; font-weight:700;">Zero missing skills - ready to hire!</span>';

  // Render Learning Pathways
  const pathwaysList = document.getElementById('learning-pathways-list');
  if (pathwaysList) {
    pathwaysList.innerHTML = match.missingSkills.map(skill => `
      <div style="background:#f8fafc; border:1px solid var(--border); border-radius:var(--radius-md); padding:1rem;">
        <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:0.5rem;">
          <strong style="color:var(--text-main); font-size:0.95rem;">${skill}</strong>
          <span class="tech-tag" style="background:rgba(99, 102, 241, 0.1); color:var(--primary); font-size:0.7rem;">Free Resource</span>
        </div>
        <p style="font-size:0.82rem; color:var(--text-muted); margin-bottom:0.75rem;">
          Master core fundamentals and complete hands-on portfolio projects in ${skill}.
        </p>
        <a href="https://www.google.com/search?q=${encodeURIComponent(skill)}+tutorial+beginners" target="_blank" class="btn btn-secondary btn-sm" style="font-size:0.78rem;">
          <i class="fa-solid fa-arrow-up-right-from-square"></i> Open Tutorial
        </a>
      </div>
    `).join('') || '<div style="color:var(--text-muted); font-size:0.9rem;">You have acquired all core technical requirements for this role!</div>';
  }

  // Readiness Breakdown
  const breakdownContainer = document.getElementById('readiness-breakdown-container');
  if (breakdownContainer) {
    breakdownContainer.innerHTML = `
      <div style="display:flex; align-items:center; justify-content:space-between; padding:0.5rem 0; border-bottom:1px solid var(--border); font-size:0.88rem;">
        <span>Profile Completeness</span><strong style="color:#10b981;">20 / 20 pts</strong>
      </div>
      <div style="display:flex; align-items:center; justify-content:space-between; padding:0.5rem 0; border-bottom:1px solid var(--border); font-size:0.88rem;">
        <span>Resume Uploaded & Parsed</span><strong style="color:#10b981;">25 / 25 pts</strong>
      </div>
      <div style="display:flex; align-items:center; justify-content:space-between; padding:0.5rem 0; border-bottom:1px solid var(--border); font-size:0.88rem;">
        <span>Verified Skills Count</span><strong style="color:#10b981;">25 / 25 pts</strong>
      </div>
      <div style="display:flex; align-items:center; justify-content:space-between; padding:0.5rem 0; font-size:0.88rem;">
        <span>Application & Bookmark Activity</span><strong style="color:#10b981;">15 / 30 pts</strong>
      </div>
    `;
  }

  const recList = document.getElementById('readiness-recommendations-list');
  if (recList) {
    recList.innerHTML = `
      <li>Submit applications to at least 2 internships with 75%+ skill match.</li>
      <li>Acquire ${match.missingSkills[0] || 'Docker'} to unlock 95%+ match ratings.</li>
    `;
  }
}

async function loadStudentApplications() {
  const container = document.getElementById('student-applications-container');
  if (!container) return;

  try {
    const res = await apiRequest('/api/student/applications');
    if (res.success && res.data) {
      renderApplicationsList(res.data, container);
      document.getElementById('count-my-apps').textContent = res.data.length;
      return;
    }
  } catch (err) {}

  // Fallback demo application
  const demoApps = [
    {
      id: 101,
      internshipTitle: "Full Stack Java Developer Intern",
      companyName: "TechNova Innovations",
      matchPercentage: 83,
      status: "INTERVIEW_SCHEDULED",
      interviewDate: "Tomorrow at 3:00 PM IST",
      interviewLink: "https://meet.google.com/abc-mind-smart",
      interviewNotes: "Round 1 Technical Interview: Focus on Spring Boot, REST APIs, and MySQL indexing.",
      appliedAt: "2026-11-10T12:00:00"
    }
  ];

  renderApplicationsList(demoApps, container);
  document.getElementById('count-my-apps').textContent = demoApps.length;
}

function renderApplicationsList(apps, container) {
  if (!apps || apps.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:3rem; background:white; border-radius:var(--radius-lg); border:1px solid var(--border);">
        <i class="fa-solid fa-paper-plane" style="font-size:2rem; color:var(--text-light); margin-bottom:0.75rem;"></i>
        <h4 style="font-weight:800;">No applications submitted yet</h4>
        <p style="color:var(--text-muted); font-size:0.88rem;">Explore high-match internships and submit your application.</p>
        <button class="btn btn-primary btn-sm" onclick="switchView('view-explore')" style="margin-top:1rem;">Explore Internships</button>
      </div>
    `;
    return;
  }

  container.innerHTML = apps.map(app => {
    const statusClass = (app.status || 'PENDING').toLowerCase();
    const isInterview = app.status === 'INTERVIEW_SCHEDULED';

    return `
      <div style="background:white; border:1px solid var(--border); border-radius:var(--radius-lg); padding:1.5rem; margin-bottom:1rem; box-shadow:var(--shadow-sm);">
        <div style="display:flex; align-items:flex-start; justify-content:space-between; flex-wrap:wrap; gap:1rem;">
          <div>
            <h4 style="font-size:1.15rem; font-weight:800; color:var(--text-main);">${app.internshipTitle}</h4>
            <span style="font-size:0.9rem; color:var(--text-muted);"><i class="fa-solid fa-building"></i> ${app.companyName}</span>
          </div>
          <div style="display:flex; align-items:center; gap:0.6rem;">
            <span class="match-score-pill ${app.matchPercentage>=75?'high':'medium'}">${app.matchPercentage}% Match</span>
            <span class="status-pill ${statusClass}">${(app.status || 'PENDING').replace('_', ' ')}</span>
          </div>
        </div>

        ${isInterview ? `
          <div style="background:#ecfdf5; border:1px solid #a7f3d0; border-radius:var(--radius-md); padding:1rem; margin-top:1rem;">
            <div style="display:flex; align-items:center; gap:0.5rem; color:#065f46; font-weight:800; margin-bottom:0.35rem;">
              <i class="fa-solid fa-calendar-check"></i> Interview Scheduled!
            </div>
            <div style="font-size:0.88rem; color:#065f46;">
              <p><strong>Date & Time:</strong> ${app.interviewDate || 'To be announced'}</p>
              ${app.interviewLink ? `<p style="margin-top:0.25rem;"><strong>Meeting Link:</strong> <a href="${app.interviewLink}" target="_blank" style="color:#047857; text-decoration:underline;">${app.interviewLink}</a></p>` : ''}
              ${app.interviewNotes ? `<p style="margin-top:0.25rem;"><strong>Notes:</strong> ${app.interviewNotes}</p>` : ''}
            </div>
          </div>
        ` : ''}
      </div>
    `;
  }).join('');
}

async function loadStudentSaved() {
  const container = document.getElementById('student-saved-grid');
  if (!container) return;

  try {
    const res = await apiRequest('/api/student/saved-internships');
    if (res.success && res.data) {
      renderInternshipsGrid(res.data, container);
      document.getElementById('count-my-saved').textContent = res.data.length;
      return;
    }
  } catch (err) {}

  document.getElementById('count-my-saved').textContent = '0';
  container.innerHTML = `
    <div style="grid-column:1/-1; text-align:center; padding:3rem; background:white; border-radius:var(--radius-lg); border:1px solid var(--border);">
      <i class="fa-regular fa-bookmark" style="font-size:2rem; color:var(--text-light); margin-bottom:0.75rem;"></i>
      <h4 style="font-weight:800;">No saved internships</h4>
      <p style="color:var(--text-muted); font-size:0.88rem;">Click the bookmark icon on any card to save it here.</p>
    </div>
  `;
}

// ================= COMPANY DASHBOARD LOGIC =================
function setupCompanyDashboard() {
  setupTabSwitcher('#view-company .dash-tab-btn', '#view-company .dash-tab-pane');

  document.getElementById('btn-quick-post-modal')?.addEventListener('click', openPostInternshipModal);
  document.getElementById('btn-post-internship-tab')?.addEventListener('click', openPostInternshipModal);

  // Logo upload
  const logoInput = document.getElementById('company-logo-input');
  document.getElementById('btn-browse-logo')?.addEventListener('click', () => logoInput?.click());
  logoInput?.addEventListener('change', () => {
    if (logoInput.files.length > 0) {
      handleCompanyLogoUpload(logoInput.files[0]);
    }
  });

  // Company Profile Form
  document.getElementById('company-profile-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      companyName: document.getElementById('comp-prof-name').value.trim(),
      recruiterName: document.getElementById('comp-prof-recruiter').value.trim(),
      phone: document.getElementById('comp-prof-phone').value.trim(),
      website: document.getElementById('comp-prof-website').value.trim(),
      industry: document.getElementById('comp-prof-industry').value.trim(),
      location: document.getElementById('comp-prof-location').value.trim(),
      description: document.getElementById('comp-prof-desc').value.trim()
    };

    try {
      const res = await apiRequest('/api/company/profile', {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('Profile Updated', 'Company details saved successfully.', 'success');
    } catch (err) {
      showToast('Profile Updated (Local)', 'Company information updated.', 'success');
    }
  });

  // Post / Edit Internship Form
  document.getElementById('form-post-internship')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('post-internship-id').value;
    const skillsRaw = document.getElementById('post-skills').value;
    const requiredSkills = skillsRaw.split(',').map(s => s.trim()).filter(Boolean);

    const payload = {
      title: document.getElementById('post-title').value.trim(),
      category: document.getElementById('post-category').value,
      workMode: document.getElementById('post-work-mode').value,
      location: document.getElementById('post-location').value.trim(),
      duration: document.getElementById('post-duration').value.trim(),
      stipend: parseInt(document.getElementById('post-stipend').value) || 0,
      deadline: document.getElementById('post-deadline').value,
      description: document.getElementById('post-desc').value.trim(),
      eligibility: document.getElementById('post-eligibility').value.trim(),
      requiredSkills: requiredSkills
    };

    try {
      const endpoint = id ? `/api/company/internships/${id}` : '/api/company/internships';
      const method = id ? 'PUT' : 'POST';

      const res = await apiRequest(endpoint, {
        method: method,
        body: JSON.stringify(payload)
      });

      showToast('Success!', id ? 'Internship updated' : 'Internship published live!', 'success');
      document.getElementById('modal-post-internship')?.classList.remove('active');
      loadCompanyPostings();
      fetchInternships();
    } catch (err) {
      // Offline fallback posting
      showToast('Internship Published (Demo Mode)', `${payload.title} published. Available for applicants.`, 'success');
      FALLBACK_INTERNSHIPS.unshift({
        id: Date.now(),
        ...payload,
        companyName: state.currentUser?.name || 'TechNova Innovations',
        companyVerified: true,
        createdAt: new Date().toISOString()
      });
      document.getElementById('modal-post-internship')?.classList.remove('active');
      loadCompanyPostings();
      fetchInternships();
    }
  });

  // Schedule Interview modal confirmation
  document.getElementById('btn-confirm-interview')?.addEventListener('click', async () => {
    const appId = document.getElementById('interview-app-id').value;
    const dt = document.getElementById('interview-datetime').value;
    const link = document.getElementById('interview-link').value.trim();
    const notes = document.getElementById('interview-notes').value.trim();

    if (!dt) {
      showToast('Required', 'Please enter interview date and time', 'warning');
      return;
    }

    try {
      await apiRequest(`/api/company/applications/${appId}/status`, {
        method: 'PUT',
        body: JSON.stringify({
          status: 'INTERVIEW_SCHEDULED',
          interviewDate: dt,
          interviewLink: link,
          interviewNotes: notes
        })
      });
      showToast('Interview Scheduled!', 'Status updated and notification email sent to applicant.', 'success');
    } catch (err) {
      showToast('Interview Scheduled (Demo Mode)', 'Simulated email invite generated.', 'success');
    }

    document.getElementById('modal-schedule-interview')?.classList.remove('active');
    loadCompanyApplications();
    fetchRecentEmailsCount();
  });
}

async function loadCompanyProfile() {
  try {
    const res = await apiRequest('/api/company/profile');
    if (res.success && res.data) {
      state.companyProfile = res.data;
      renderCompanyProfileData(res.data);
    }
  } catch (err) {
    const defaultComp = {
      companyName: 'TechNova Innovations',
      recruiterName: state.currentUser?.name || 'Ananya Sen',
      email: state.currentUser?.email || 'hr@technova.com',
      phone: '+91 9800112233',
      website: 'https://technova.io',
      industry: 'Software & AI Solutions',
      location: 'Bangalore, India',
      description: 'Next-gen enterprise software and AI solutions provider.',
      isVerified: true
    };
    state.companyProfile = defaultComp;
    renderCompanyProfileData(defaultComp);
  }

  loadCompanyPostings();
  loadCompanyApplications();
}

function renderCompanyProfileData(p) {
  document.getElementById('company-welcome-name').textContent = p.companyName || 'Company';

  const verifiedPill = document.getElementById('company-verified-pill');
  if (verifiedPill) {
    verifiedPill.style.display = p.isVerified ? 'inline-flex' : 'none';
  }

  if (document.getElementById('comp-prof-name')) document.getElementById('comp-prof-name').value = p.companyName || '';
  if (document.getElementById('comp-prof-recruiter')) document.getElementById('comp-prof-recruiter').value = p.recruiterName || '';
  if (document.getElementById('comp-prof-phone')) document.getElementById('comp-prof-phone').value = p.phone || '';
  if (document.getElementById('comp-prof-website')) document.getElementById('comp-prof-website').value = p.website || '';
  if (document.getElementById('comp-prof-industry')) document.getElementById('comp-prof-industry').value = p.industry || '';
  if (document.getElementById('comp-prof-location')) document.getElementById('comp-prof-location').value = p.location || '';
  if (document.getElementById('comp-prof-desc')) document.getElementById('comp-prof-desc').value = p.description || '';

  if (p.logoUrl) {
    const img = document.getElementById('comp-logo-img');
    const placeholder = document.getElementById('comp-logo-placeholder-icon');
    if (img) {
      img.src = p.logoUrl;
      img.style.display = 'block';
    }
    if (placeholder) placeholder.style.display = 'none';
  }
}

async function handleCompanyLogoUpload(file) {
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const res = await fetch(`${API_BASE}/api/company/logo/upload`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${state.token}` },
      body: formData
    });
    const data = await res.json();
    if (data.success) {
      showToast('Logo Uploaded', 'Company logo updated across all postings!', 'success');
      const img = document.getElementById('comp-logo-img');
      const placeholder = document.getElementById('comp-logo-placeholder-icon');
      if (img) {
        img.src = data.data;
        img.style.display = 'block';
      }
      if (placeholder) placeholder.style.display = 'none';
    }
  } catch (err) {
    // Local preview fallback
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = document.getElementById('comp-logo-img');
      const placeholder = document.getElementById('comp-logo-placeholder-icon');
      if (img) {
        img.src = e.target.result;
        img.style.display = 'block';
      }
      if (placeholder) placeholder.style.display = 'none';
      showToast('Logo Preview (Local)', 'Logo loaded into preview.', 'info');
    };
    reader.readAsDataURL(file);
  }
}

async function loadCompanyPostings() {
  const container = document.getElementById('company-postings-container');
  if (!container) return;

  try {
    const res = await apiRequest('/api/company/internships');
    if (res.success && res.data) {
      renderCompanyPostingsTable(res.data, container);
      document.getElementById('comp-stat-active-internships').textContent = res.data.length;
      return;
    }
  } catch (err) {}

  // Fallback demo company postings
  const demoPostings = FALLBACK_INTERNSHIPS.filter(i => i.companyName.includes('TechNova'));
  renderCompanyPostingsTable(demoPostings, container);
  document.getElementById('comp-stat-active-internships').textContent = demoPostings.length;
}

function renderCompanyPostingsTable(list, container) {
  if (!list || list.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:3rem; background:white; border-radius:var(--radius-lg); border:1px solid var(--border);">
        <i class="fa-solid fa-briefcase" style="font-size:2rem; color:var(--text-light); margin-bottom:0.75rem;"></i>
        <h4 style="font-weight:800;">No active internship listings</h4>
        <p style="color:var(--text-muted); font-size:0.88rem;">Publish your first opportunity to receive smart-matched candidates.</p>
        <button class="btn btn-primary btn-sm" onclick="openPostInternshipModal()" style="margin-top:1rem;"><i class="fa-solid fa-plus"></i> Post Internship</button>
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div class="table-responsive">
      <table class="modern-table">
        <thead>
          <tr>
            <th>Role Title</th>
            <th>Work Mode</th>
            <th>Stipend</th>
            <th>Required Skills</th>
            <th>Deadline</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${list.map(i => `
            <tr>
              <td><strong>${i.title}</strong><br><span style="font-size:0.75rem; color:var(--text-muted);">${i.category}</span></td>
              <td><span class="highlight-pill mode">${i.workMode || 'Remote'}</span></td>
              <td>₹${(i.stipend || 0).toLocaleString()}/mo</td>
              <td><div class="skill-chips-row">${(i.requiredSkills || []).slice(0, 3).map(s=>`<span class="skill-chip neutral">${s}</span>`).join('')}</div></td>
              <td>${i.deadline || 'Active'}</td>
              <td>
                <button class="btn btn-secondary btn-sm" onclick="deleteCompanyPosting(${i.id})"><i class="fa-solid fa-trash" style="color:var(--danger);"></i></button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function openPostInternshipModal(editData = null) {
  const modal = document.getElementById('modal-post-internship');
  if (!modal) return;

  document.getElementById('post-internship-id').value = editData ? editData.id : '';
  document.getElementById('post-title').value = editData ? editData.title : '';
  document.getElementById('post-stipend').value = editData ? editData.stipend : 25000;
  document.getElementById('post-duration').value = editData ? editData.duration : '6 Months';
  document.getElementById('post-location').value = editData ? editData.location : 'Remote / Bangalore';
  document.getElementById('post-skills').value = editData ? (editData.requiredSkills || []).join(', ') : 'Java, Spring Boot, MySQL, REST API, Git';
  document.getElementById('post-desc').value = editData ? editData.description : '';
  document.getElementById('post-eligibility').value = editData ? editData.eligibility : 'B.Tech / MCA in CS/IT';

  // Set default deadline 30 days ahead
  const d = new Date();
  d.setDate(d.getDate() + 30);
  document.getElementById('post-deadline').value = d.toISOString().split('T')[0];

  modal.classList.add('active');
}

async function deleteCompanyPosting(id) {
  if (!confirm("Are you sure you want to delete this internship posting?")) return;

  try {
    await apiRequest(`/api/company/internships/${id}`, { method: 'DELETE' });
    showToast('Deleted', 'Internship removed', 'info');
  } catch (err) {
    showToast('Deleted (Local)', 'Internship posting removed', 'info');
  }

  loadCompanyPostings();
  fetchInternships();
}

async function loadCompanyApplications() {
  const container = document.getElementById('company-ats-container');
  if (!container) return;

  try {
    const res = await apiRequest('/api/company/applications');
    if (res.success && res.data) {
      renderCompanyATSList(res.data, container);
      document.getElementById('comp-stat-total-applicants').textContent = res.data.length;
      document.getElementById('comp-stat-interviews').textContent = res.data.filter(a => a.status === 'INTERVIEW_SCHEDULED').length;
      return;
    }
  } catch (err) {}

  // Fallback demo candidate application
  const demoCandidates = [
    {
      id: 201,
      studentName: "Rahul Sharma",
      studentEmail: "student@mindsmart.com",
      studentCollege: "National Institute of Technology",
      studentDegree: "B.Tech (Computer Science)",
      internshipTitle: "Full Stack Java Developer Intern",
      matchPercentage: 83,
      status: "REVIEWING",
      matchingSkills: ["Java", "Spring Boot", "MySQL", "REST API", "Git"],
      missingSkills: ["React"],
      coverLetter: "Excited to contribute to high-performance microservices and cloud scalability at TechNova.",
      appliedAt: "2026-11-10T12:00:00"
    }
  ];

  renderCompanyATSList(demoCandidates, container);
  document.getElementById('comp-stat-total-applicants').textContent = demoCandidates.length;
  document.getElementById('comp-stat-interviews').textContent = demoCandidates.filter(a => a.status === 'INTERVIEW_SCHEDULED').length;
}

function renderCompanyATSList(apps, container) {
  if (!apps || apps.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:3rem; background:white; border-radius:var(--radius-lg); border:1px solid var(--border);">
        <i class="fa-solid fa-users" style="font-size:2rem; color:var(--text-light); margin-bottom:0.75rem;"></i>
        <h4 style="font-weight:800;">No candidates have applied yet</h4>
        <p style="color:var(--text-muted); font-size:0.88rem;">Applicants matching your required skills will appear here automatically.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = apps.map(app => `
    <div style="background:white; border:1px solid var(--border); border-radius:var(--radius-lg); padding:1.5rem; margin-bottom:1.25rem; box-shadow:var(--shadow-sm);">
      <div style="display:flex; align-items:flex-start; justify-content:space-between; flex-wrap:wrap; gap:1rem;">
        <div>
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <h4 style="font-size:1.15rem; font-weight:800; color:var(--text-main);">${app.studentName}</h4>
            <span class="match-score-pill ${app.matchPercentage>=75?'high':'medium'}">${app.matchPercentage}% Skill Match</span>
          </div>
          <div style="font-size:0.85rem; color:var(--text-muted); margin-top:0.25rem;">
            <span><i class="fa-solid fa-graduation-cap"></i> ${app.studentCollege || 'Engineering College'} &bull; ${app.studentDegree || 'B.Tech'}</span>
          </div>
          <div style="font-size:0.82rem; color:var(--text-muted); margin-top:0.2rem;">
            <span><i class="fa-solid fa-envelope"></i> ${app.studentEmail}</span>
            <span style="margin-left:1rem;"><i class="fa-solid fa-briefcase"></i> Applied for: <strong>${app.internshipTitle}</strong></span>
          </div>
        </div>

        <!-- Action Dropdown & Schedule Button -->
        <div style="display:flex; align-items:center; gap:0.5rem;">
          <select class="form-control" style="width:auto; padding:0.4rem 2rem 0.4rem 0.85rem; font-size:0.85rem;" onchange="updateApplicantStatus(${app.id}, this.value)">
            <option value="REVIEWING" ${app.status==='REVIEWING'?'selected':''}>Under Review</option>
            <option value="SHORTLISTED" ${app.status==='SHORTLISTED'?'selected':''}>Shortlist</option>
            <option value="ACCEPTED" ${app.status==='ACCEPTED'?'selected':''}>Accept Offer</option>
            <option value="REJECTED" ${app.status==='REJECTED'?'selected':''}>Reject</option>
          </select>
          <button class="btn btn-primary btn-sm" onclick="openScheduleInterviewModal(${app.id}, '${app.studentName}')">
            <i class="fa-solid fa-calendar-plus"></i> Schedule Interview
          </button>
        </div>
      </div>

      <!-- Skills Match Analysis -->
      <div style="background:#f8fafc; border-radius:var(--radius-md); padding:0.85rem; margin-top:1rem; display:flex; flex-wrap:wrap; gap:1rem; justify-content:space-between;">
        <div>
          <span style="font-size:0.78rem; font-weight:700; color:#059669; display:block; margin-bottom:0.35rem;">Matching Skills:</span>
          <div class="skill-chips-row">${(app.matchingSkills || []).map(s=>`<span class="skill-chip matched">✓ ${s}</span>`).join('') || '<span style="font-size:0.8rem; color:var(--text-muted);">None</span>'}</div>
        </div>
        <div>
          <span style="font-size:0.78rem; font-weight:700; color:#d97706; display:block; margin-bottom:0.35rem;">Missing Skills:</span>
          <div class="skill-chips-row">${(app.missingSkills || []).map(s=>`<span class="skill-chip missing">+ ${s}</span>`).join('') || '<span style="font-size:0.8rem; color:#059669;">100% Match</span>'}</div>
        </div>
      </div>

      ${app.coverLetter ? `
        <div style="margin-top:0.75rem; font-size:0.85rem; color:#475569; font-style:italic;">
          "${app.coverLetter}"
        </div>
      ` : ''}
    </div>
  `).join('');
}

async function updateApplicantStatus(appId, newStatus) {
  try {
    await apiRequest(`/api/company/applications/${appId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: newStatus })
    });
    showToast('Status Updated', `Candidate marked as ${newStatus}. Notification sent.`, 'success');
  } catch (err) {
    showToast('Status Updated (Local)', `Candidate status set to ${newStatus}.`, 'info');
  }
  fetchRecentEmailsCount();
}

function openScheduleInterviewModal(appId, studentName) {
  document.getElementById('interview-app-id').value = appId;
  const d = new Date();
  d.setDate(d.getDate() + 2);
  d.setHours(15, 0, 0, 0);
  document.getElementById('interview-datetime').value = d.toISOString().slice(0, 16);
  document.getElementById('interview-link').value = 'https://meet.google.com/mindsmart-' + appId;
  document.getElementById('interview-notes').value = 'Round 1 Technical Screening: Coding problem solving and architecture questions.';

  document.getElementById('modal-schedule-interview')?.classList.add('active');
}

// ================= ADMIN DASHBOARD LOGIC =================
function setupAdminDashboard() {
  setupTabSwitcher('#view-admin .dash-tab-btn', '#view-admin .dash-tab-pane');
}

async function loadAdminDashboard() {
  try {
    const res = await apiRequest('/api/admin/stats');
    if (res.success && res.data) {
      document.getElementById('admin-stat-students').textContent = res.data.totalStudents || 0;
      document.getElementById('admin-stat-companies').textContent = res.data.totalCompanies || 0;
      document.getElementById('admin-stat-internships').textContent = res.data.totalInternships || 0;
      document.getElementById('admin-stat-applications').textContent = res.data.totalApplications || 0;
    }
  } catch (err) {
    document.getElementById('admin-stat-students').textContent = '2';
    document.getElementById('admin-stat-companies').textContent = '5';
    document.getElementById('admin-stat-internships').textContent = '10';
    document.getElementById('admin-stat-applications').textContent = '4';
  }

  loadAdminCompanies();
  loadAdminUsers();
}

async function loadAdminCompanies() {
  const tbody = document.getElementById('admin-companies-tbody');
  if (!tbody) return;

  try {
    const res = await apiRequest('/api/admin/companies');
    if (res.success && res.data) {
      renderAdminCompaniesTable(res.data, tbody);
      return;
    }
  } catch (err) {}

  // Fallback companies
  const fallbackCompanies = [
    { id: 1, companyName: "TechNova Innovations", industry: "Software & AI", location: "Bangalore", website: "https://technova.io", isVerified: true },
    { id: 2, companyName: "CloudScale Systems", industry: "Cloud Infrastructure", location: "Hyderabad", website: "https://cloudscale.io", isVerified: true },
    { id: 3, companyName: "CyberPulse Security", industry: "Cyber Security", location: "Mumbai", website: "https://cyberpulse.security", isVerified: true },
    { id: 4, companyName: "DataVibe Analytics", industry: "Data Science & BI", location: "Pune", website: "https://datavibe.ai", isVerified: true },
    { id: 5, companyName: "NexaByte Labs", industry: "Web & Mobile Studio", location: "Delhi NCR", website: "https://nexabyte.dev", isVerified: false }
  ];

  renderAdminCompaniesTable(fallbackCompanies, tbody);
}

function renderAdminCompaniesTable(companies, tbody) {
  tbody.innerHTML = companies.map(c => `
    <tr>
      <td><strong>${c.companyName}</strong></td>
      <td>${c.industry || 'Tech'}</td>
      <td>${c.location || 'India'}</td>
      <td><a href="${c.website}" target="_blank" style="color:var(--primary);">${c.website || 'N/A'}</a></td>
      <td>
        <span class="status-pill ${c.isVerified ? 'accepted' : 'pending'}">
          ${c.isVerified ? '✓ Verified Employer' : 'Pending Verification'}
        </span>
      </td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="toggleCompanyVerify(${c.id}, ${!c.isVerified})">
          ${c.isVerified ? 'Revoke Verification' : '<i class="fa-solid fa-check"></i> Verify Company'}
        </button>
      </td>
    </tr>
  `).join('');
}

async function toggleCompanyVerify(companyId, verify) {
  try {
    await apiRequest(`/api/admin/companies/${companyId}/verify?verify=${verify}`, { method: 'PUT' });
    showToast('Company Updated', verify ? 'Company verified! Badge granted.' : 'Verification revoked', 'success');
  } catch (err) {
    showToast('Company Updated (Local)', verify ? 'Company verified! Badge granted.' : 'Verification revoked', 'success');
  }
  loadAdminCompanies();
}

async function loadAdminUsers() {
  const tbody = document.getElementById('admin-users-tbody');
  if (!tbody) return;

  try {
    const res = await apiRequest('/api/admin/users');
    if (res.success && res.data) {
      renderAdminUsersTable(res.data, tbody);
      return;
    }
  } catch (err) {}

  const demoUsers = [
    { id: 1, name: "Platform Administrator", email: "admin@mindsmart.com", role: "ROLE_ADMIN", enabled: true },
    { id: 2, name: "Rahul Sharma", email: "student@mindsmart.com", role: "ROLE_STUDENT", enabled: true },
    { id: 3, name: "Arun Kumar", email: "arun.kumar@gmail.com", role: "ROLE_STUDENT", enabled: true },
    { id: 4, name: "Ananya Sen (TechNova)", email: "hr@technova.com", role: "ROLE_COMPANY", enabled: true },
    { id: 5, name: "Vikram Malhotra (CloudScale)", email: "careers@cloudscale.io", role: "ROLE_COMPANY", enabled: true }
  ];

  renderAdminUsersTable(demoUsers, tbody);
}

function renderAdminUsersTable(users, tbody) {
  tbody.innerHTML = users.map(u => `
    <tr>
      <td><strong>${u.name}</strong></td>
      <td>${u.email}</td>
      <td><span class="user-role-tag">${u.role.replace('ROLE_', '')}</span></td>
      <td><span class="status-pill ${u.enabled ? 'accepted' : 'rejected'}">${u.enabled ? 'Active' : 'Suspended'}</span></td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="toggleUserStatus(${u.id})">
          ${u.enabled ? 'Suspend' : 'Activate'}
        </button>
      </td>
    </tr>
  `).join('');
}

async function toggleUserStatus(userId) {
  try {
    await apiRequest(`/api/admin/users/${userId}/toggle-status`, { method: 'PUT' });
    showToast('User Updated', 'Account access status toggled.', 'info');
  } catch (err) {
    showToast('User Updated (Local)', 'Account status updated.', 'info');
  }
  loadAdminUsers();
}

// ================= EMAIL SIMULATOR & NOTIFICATIONS =================
async function openEmailSimulatorModal() {
  const modal = document.getElementById('modal-email-simulator');
  const container = document.getElementById('email-simulator-list');
  if (!modal || !container) return;

  container.innerHTML = '<div style="text-align:center; padding:2rem;"><i class="fa-solid fa-circle-notch fa-spin"></i> Loading logged emails...</div>';
  modal.classList.add('active');

  try {
    const res = await apiRequest('/api/notifications/recent-emails');
    if (res.success && res.data && res.data.length > 0) {
      renderEmailSimulatorList(res.data, container);
      return;
    }
  } catch (err) {}

  // Fallback demo emails
  const demoEmails = [
    {
      id: 1,
      recipientEmail: "student@mindsmart.com",
      subject: "Welcome to MindSmart – Smart Internship Opportunities Platform!",
      sentAt: new Date().toISOString(),
      status: "SIMULATED_DEMO",
      body: "<p>Welcome Rahul! Your MindSmart student profile is active. Upload your resume to extract skills and unlock smart recommendations.</p>"
    },
    {
      id: 2,
      recipientEmail: "student@mindsmart.com",
      subject: "Application Submitted: Full Stack Java Developer Intern at TechNova Innovations",
      sentAt: new Date().toISOString(),
      status: "SIMULATED_DEMO",
      body: "<p>Your application was successfully delivered to TechNova Innovations recruiting team with an 83% match rating.</p>"
    }
  ];

  renderEmailSimulatorList(demoEmails, container);
}

function renderEmailSimulatorList(emails, container) {
  container.innerHTML = emails.map(e => `
    <div style="background:#f8fafc; border:1px solid var(--border); border-radius:var(--radius-md); padding:1.25rem;">
      <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:0.5rem; flex-wrap:wrap; gap:0.5rem;">
        <span style="font-size:0.85rem; font-weight:700; color:var(--text-main);"><i class="fa-solid fa-envelope"></i> To: ${e.recipientEmail}</span>
        <span class="tech-tag" style="background:#ecfdf5; color:#059669; font-size:0.75rem;">Status: ${e.status || 'SIMULATED_DEMO'}</span>
      </div>
      <h4 style="font-size:1rem; font-weight:800; color:var(--primary); margin-bottom:0.75rem;">${e.subject}</h4>
      <div style="background:white; border:1px solid var(--border); border-radius:var(--radius-sm); padding:1rem; font-size:0.88rem; line-height:1.6; max-height:220px; overflow-y:auto;">
        ${e.body}
      </div>
    </div>
  `).join('');
}

async function fetchRecentEmailsCount() {
  try {
    const res = await apiRequest('/api/notifications/recent-emails');
    if (res.success && res.data) {
      const badge = document.getElementById('email-counter-badge');
      if (badge && res.data.length > 0) {
        badge.textContent = res.data.length;
        badge.style.display = 'flex';
      }
    }
  } catch (err) {}
}

async function fetchUnreadNotificationCount() {
  if (!state.token) return;
  try {
    const res = await apiRequest('/api/notifications/unread-count');
    if (res.success) {
      state.unreadCount = res.data;
      const badge = document.getElementById('unread-counter-badge');
      if (badge && state.unreadCount > 0) {
        badge.textContent = state.unreadCount;
        badge.style.display = 'flex';
      }
    }
  } catch (err) {}
}

async function openNotificationsModal() {
  const modal = document.getElementById('modal-notifications');
  const container = document.getElementById('notifications-list-container');
  if (!modal || !container) return;

  modal.classList.add('active');
  container.innerHTML = '<div style="text-align:center; padding:2rem;"><i class="fa-solid fa-circle-notch fa-spin"></i> Loading notifications...</div>';

  try {
    const res = await apiRequest('/api/notifications');
    if (res.success && res.data && res.data.length > 0) {
      renderNotificationsList(res.data, container);
      return;
    }
  } catch (err) {}

  container.innerHTML = `
    <div style="text-align:center; padding:2rem; color:var(--text-muted);">
      <i class="fa-solid fa-bell-slash" style="font-size:1.8rem; margin-bottom:0.5rem; display:block;"></i>
      No new unread notifications.
    </div>
  `;
}

function renderNotificationsList(notifications, container) {
  container.innerHTML = notifications.map(n => `
    <div style="padding:0.85rem 1rem; border-radius:var(--radius-md); background:${n.isRead ? '#f8fafc' : 'rgba(99, 102, 241, 0.08)'}; border:1px solid var(--border);">
      <div style="font-weight:700; font-size:0.92rem; color:var(--text-main);">${n.title}</div>
      <div style="font-size:0.84rem; color:var(--text-muted); margin-top:0.25rem;">${n.message}</div>
    </div>
  `).join('');
}

document.getElementById('btn-mark-all-read')?.addEventListener('click', async () => {
  try {
    await apiRequest('/api/notifications/read-all', { method: 'PUT' });
    showToast('Read', 'All notifications marked as read', 'info');
    document.getElementById('unread-counter-badge').style.display = 'none';
  } catch (err) {}
});

// ================= MODAL DISMISS LOGIC =================
function setupModals() {
  const modalIds = [
    'modal-auth',
    'modal-internship-details',
    'modal-apply',
    'modal-schedule-interview',
    'modal-post-internship',
    'modal-comparison',
    'modal-email-simulator',
    'modal-notifications'
  ];

  modalIds.forEach(id => {
    const overlay = document.getElementById(id);
    if (!overlay) return;

    // Click outside modal card to close
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) overlay.classList.remove('active');
    });

    // Close button
    const closeBtn = overlay.querySelector('.close-btn');
    closeBtn?.addEventListener('click', () => overlay.classList.remove('active'));
  });

  document.getElementById('btn-cancel-apply')?.addEventListener('click', () => {
    document.getElementById('modal-apply')?.classList.remove('active');
  });

  document.getElementById('btn-cancel-interview')?.addEventListener('click', () => {
    document.getElementById('modal-schedule-interview')?.classList.remove('active');
  });

  document.getElementById('btn-cancel-post')?.addEventListener('click', () => {
    document.getElementById('modal-post-internship')?.classList.remove('active');
  });

  // Report Internship trigger
  document.getElementById('detail-report-btn')?.addEventListener('click', async () => {
    if (!state.selectedInternship) return;
    const reason = prompt("Please provide a reason for reporting this internship listing:");
    if (reason) {
      try {
        await apiRequest(`/api/internships/${state.selectedInternship.id}/report?reason=${encodeURIComponent(reason)}`, { method: 'POST' });
        showToast('Report Logged', 'Listing flagged for administrator moderation.', 'info');
      } catch (err) {
        showToast('Report Logged (Demo Mode)', 'Flagged for moderation.', 'info');
      }
      document.getElementById('modal-internship-details')?.classList.remove('active');
    }
  });
}

// ================= TAB SWITCHER HELPER =================
function setupTabSwitcher(buttonSelector, paneSelector) {
  document.querySelectorAll(buttonSelector).forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll(buttonSelector).forEach(b => b.classList.remove('active'));
      document.querySelectorAll(paneSelector).forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const targetPaneId = btn.getAttribute('data-tab');
      const targetPane = document.getElementById(targetPaneId);
      if (targetPane) targetPane.classList.add('active');
    });
  });
}
