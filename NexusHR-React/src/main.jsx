import React, { useEffect, useState } from "react";

import { createRoot } from "react-dom/client";

import {

  LayoutDashboard,

  Users,

  CalendarDays,

  WalletCards,

  Target,

  Sparkles,

  FileText,

  Settings,

  Search,

  Bell,

  ChevronDown,

  ChevronLeft,

  ChevronRight,

  Plus,

  Check,

  X,

  ArrowUpRight,

  Menu,

  LogOut,

  Eye,

  MoreHorizontal,

  Clock3,

  CircleAlert,

  CheckCircle2,

  UserRound,

  BriefcaseBusiness,

  FileCheck2,

  IndianRupee,

  BrainCircuit,

  Home,

  ClipboardList,

  Sun,

  Moon,

} from "lucide-react";

import "./styles.css";



const navItems = [

  ["Dashboard", "dashboard", LayoutDashboard],

  ["Employees", "employees", Users],

  ["Attendance & Leave", "attendance", CalendarDays],

  ["Payroll", "payroll", WalletCards],

  ["Performance", "performance", Target],

  ["AI Insights", "ai", Sparkles],

  ["Reports", "reports", FileText],

  ["Settings", "settings", Settings],

];



function Logo() {

  return (

    <div className="logo">

      <span className="logo-mark">N</span>

      <span>NexusHR</span>

    </div>

  );

}



function Sidebar({ page, setPage }) {

  return (

    <aside className="sidebar">

      <Logo />

      <div className="org-box">

        <small>ORGANIZATION</small>

        <strong>[Client Org Name]</strong>

      </div>

      <nav className="nav">

        {navItems.map(([label, key, Icon]) => (

          <button

            key={key}

            className={page === key ? "nav-item active" : "nav-item"}

            onClick={() => setPage(key)}

          >

            <Icon size={18} />

            <span>{label}</span>

          </button>

        ))}

      </nav>

      <div className="user-mini">

        <div className="avatar">PS</div>

        <div>

          <strong>Priya Sharma</strong>

          <small>HR Admin</small>

        </div>

        <ChevronDown size={16} />

      </div>

    </aside>

  );

}



function Topbar({ title, onMenu, darkMode, setDarkMode }) {

  return (

    <header className="topbar">

      <button className="mobile-menu" onClick={onMenu}>

        <Menu size={20} />

      </button>

      <div className="mobile-title">{title}</div>

      <div className="top-actions">

        <button className="icon-btn">

          <Bell size={18} />

          <span className="dot" />

        </button>

        <button

          className="icon-btn"

          onClick={() => setDarkMode(!darkMode)}

          title={darkMode ? "Switch to light mode" : "Switch to dark mode"}

        >

          {darkMode ? <Sun size={18} /> : <Moon size={18} />}

        </button>

        <div className="profile">

          <div className="avatar small">PS</div>

          <span>Priya Sharma</span>

          <ChevronDown size={15} />

        </div>

      </div>

    </header>

  );

}



function Shell({ page, setPage, children, title, darkMode, setDarkMode }) {

  const [open, setOpen] = useState(false);

  return (

    <div className="app">

      <div className={open ? "sidebar-wrap open" : "sidebar-wrap"}>

        <Sidebar

          page={page}

          setPage={(p) => {

            setPage(p);

            setOpen(false);

          }}

        />

      </div>

      <main className="main">

        <Topbar
          title={title}
          onMenu={() => setOpen(!open)}
          darkMode={darkMode}
          setDarkMode={setDarkMode}
        />

        {children}

      </main>

    </div>

  );

}



function PageHeader({ eyebrow, title, subtitle, action, onAction }) {

  return (

    <div className="page-header">

      <div>

        <div className="eyebrow">{eyebrow}</div>

        <h1>{title}</h1>

        {subtitle && <p>{subtitle}</p>}

      </div>

      {action && (

        <button className="btn primary" onClick={onAction}>

          <Plus size={16} />

          {action}

        </button>

      )}

    </div>

  );

}



function Stat({ label, value, note, tone = "" }) {

  return (

    <div className="stat-card">

      <div className="stat-label">{label}</div>

      <div className="stat-value">{value}</div>

      <div className={"stat-note " + tone}>{note}</div>

    </div>

  );

}



function Dashboard({ setPage, darkMode, setDarkMode }) {

  return (

    <Shell page="dashboard" setPage={setPage} title="Dashboard" darkMode={darkMode} setDarkMode={setDarkMode}>

      <div className="content">

        <PageHeader

          eyebrow="HR ADMIN"

          title="Good morning, Priya"

          subtitle="Here’s what needs your attention today · Sample data shown"

          action="Export"

        />

        <div className="toolbar">

          <div className="search">

            <Search size={17} />

            <input placeholder="Search" />

          </div>

          <button className="btn">

            Last 14 days <ChevronDown size={15} />

          </button>

        </div>

        <div className="stats-grid">

          <Stat

            label="Total headcount"

            value="2,418"

            note="+32 this month"

            tone="positive"

          />

          <Stat

            label="Present today"

            value="91.4%"

            note="−0.7% vs last week"

            tone="negative"

          />

          <Stat

            label="Pending approvals"

            value="37"

            note="12 overdue"

            tone="warning"

          />

          <Stat

            label="Attrition risk flagged"

            value="24"

            note="AI · updated 2h ago"

            tone="teal"

          />

        </div>

        <div className="grid-2">

          <section className="card chart-card">

            <div className="card-head">

              <div>

                <h3>Attendance rate</h3>

                <small>Last 14 days</small>

              </div>

              <span className="big-number">91.4%</span>

            </div>

            <div className="chart">

              <div className="chart-line" />

              {[68, 72, 71, 75, 74, 78, 76, 80, 77, 82, 81, 85, 83, 91].map(

                (v, i) => (

                  <span key={i} style={{ height: v + "%" }} />

                ),

              )}

            </div>

            <div className="chart-axis">

              <span>14 days ago</span>

              <span>Today</span>

            </div>

          </section>

          <section className="card">

            <div className="card-head">

              <h3>Headcount by department</h3>

              <ArrowUpRight size={16} />

            </div>

            {[

              ["Production", 812],

              ["Engineering", 436],

              ["Sales", 298],

              ["Operations", 354],

              ["Finance", 142],

              ["HR", 64],

            ].map(([n, v]) => (

              <div className="bar-row" key={n}>

                <span>{n}</span>

                <div className="bar">

                  <i style={{ width: (v / 812) * 100 + "%" }} />

                </div>

                <b>{v}</b>

              </div>

            ))}

          </section>

        </div>

        <div className="grid-2">

          <section className="card">

            <div className="card-head">

              <h3>Approvals queue</h3>

              <button className="link-btn">View all</button>

            </div>

            <Approval

              name="Arjun Mehta"

              initials="AM"

              dept="Engineering"

              item="Casual leave · 2 days"

              date="Oct 3 – Oct 4"

              status="Pending"

            />

            <Approval

              name="Kavya Rao"

              initials="KR"

              dept="Sales"

              item="Reimbursement · ₹4,200"

              date="Sep 26"

              status="Pending"

            />

            <Approval

              name="Rahul Verma"

              initials="RV"

              dept="Production"

              item="Shift swap"

              date="Sep 29"

              status="Needs info"

            />

          </section>

          <section className="card insight">

            <div className="card-head">

              <h3>

                <Sparkles size={17} /> AI insight

              </h3>

              <span className="pill teal">Advisory</span>

            </div>

            <p>

              Engineering attrition risk is rising: 9 people show low engagement

              and no promotion in 24+ months.

            </p>

            <p className="muted">

              Top drivers: overtime hours, stalled goals, below-band pay.

            </p>

            <button className="btn" onClick={() => setPage("ai")}>

              Open AI Insights <ArrowUpRight size={15} />

            </button>

          </section>

        </div>

      </div>

    </Shell>

  );

}



function Approval({ name, initials, dept, item, date, status }) {

  return (

    <div className="approval">

      <div className="avatar">{initials}</div>

      <div className="approval-main">

        <strong>{name}</strong>

        <small>{dept}</small>

        <span>

          {item} · {date}

        </span>

      </div>

      <span className={"pill " + (status === "Pending" ? "amber" : "gray")}>

        {status}

      </span>

      <div className="approval-actions">

        <button className="icon-btn success">

          <Check size={15} />

        </button>

        <button className="icon-btn danger">

          <X size={15} />

        </button>

      </div>

    </div>

  );

}



const employees = [

  [

    "AI",

    "Ananya Iyer",

    "Senior Developer",

    "EMP-10421",

    "Engineering",

    "Active",

    "Mar 2022",

  ],

  [

    "AM",

    "Arjun Mehta",

    "Tech Lead",

    "EMP-10388",

    "Engineering",

    "Active",

    "Aug 2021",

  ],

  [

    "KR",

    "Kavya Rao",

    "Account Executive",

    "EMP-11602",

    "Sales",

    "On leave",

    "Jan 2024",

  ],

  [

    "RV",

    "Rahul Verma",

    "Shift Supervisor",

    "EMP-09815",

    "Production",

    "Active",

    "Jun 2019",

  ],

  [

    "SK",

    "Sneha Kulkarni",

    "Payroll Analyst",

    "EMP-12044",

    "Finance",

    "Onboarding",

    "Sep 2026",

  ],

  [

    "VS",

    "Vikram Singh",

    "Logistics Manager",

    "EMP-08770",

    "Operations",

    "Notice period",

    "Feb 2018",

  ],

  [

    "MN",

    "Meera Nair",

    "HR Business Partner",

    "EMP-11930",

    "HR",

    "Active",

    "Jul 2025",

  ],

  [

    "FA",

    "Farhan Ali",

    "QA Engineer",

    "EMP-10977",

    "Engineering",

    "Active",

    "Nov 2022",

  ],

];



function Employees({ setPage, darkMode, setDarkMode }) {

  return (

    <Shell page="employees" setPage={setPage} title="Employees" darkMode={darkMode} setDarkMode={setDarkMode}>

      <div className="content">

        <PageHeader

          eyebrow="PEOPLE"

          title="Employees"

          subtitle="Directory, profiles and lifecycle workflows"

          action="Add employee"

        />

        <div className="toolbar">

          <div className="search">

            <Search size={17} />

            <input placeholder="Search" />

          </div>

          <div className="filters">

            <button className="btn">

              Department: All <ChevronDown size={14} />

            </button>

            <button className="btn">

              Status: All <ChevronDown size={14} />

            </button>

            <button className="btn">

              Location: All <ChevronDown size={14} />

            </button>

            <button className="btn">

              Joined: Any time <ChevronDown size={14} />

            </button>

          </div>

        </div>

        <section className="card table-card">

          <div className="table-top">

            <strong>2,418 employees</strong>

            <span>Showing 1–8 of 2,418</span>

          </div>

          <table>

            <thead>

              <tr>

                <th>Name</th>

                <th>ID</th>

                <th>Department</th>

                <th>Status</th>

                <th>Joined</th>

                <th></th>

              </tr>

            </thead>

            <tbody>

              {employees.map((e) => (

                <tr key={e[3]}>

                  <td>

                    <div className="person">

                      <span className="avatar">{e[0]}</span>

                      <div>

                        <strong>{e[1]}</strong>

                        <small>{e[2]}</small>

                      </div>

                    </div>

                  </td>

                  <td className="mono">{e[3]}</td>

                  <td>{e[4]}</td>

                  <td>

                    <span className={"pill " + statusClass(e[5])}>{e[5]}</span>

                  </td>

                  <td>{e[6]}</td>

                  <td>

                    <button className="icon-btn">

                      <MoreHorizontal size={17} />

                    </button>

                  </td>

                </tr>

              ))}

            </tbody>

          </table>

          <div className="pagination">

            <button className="btn">

              <ChevronLeft size={15} />

              Previous

            </button>

            <span>1 / 303</span>

            <button className="btn">

              Next

              <ChevronRight size={15} />

            </button>

          </div>

        </section>

        <section className="card employee-detail">

          <div className="detail-person">

            <div className="avatar large">SK</div>

            <div>

              <h3>Sneha Kulkarni</h3>

              <p>Payroll Analyst · Finance</p>

            </div>

            <button className="btn">

              Open profile <ArrowUpRight size={15} />

            </button>

          </div>

          <div className="detail-grid">

            {[

              ["Employee ID", "EMP-12044"],

              ["Manager", "Meera Nair"],

              ["Location", "Bengaluru"],

              ["Start date", "Sep 22, 2026"],

            ].map((x) => (

              <div key={x[0]}>

                <small>{x[0]}</small>

                <strong>{x[1]}</strong>

              </div>

            ))}

          </div>

          <div className="checklist">

            <h4>

              Onboarding checklist <span>3 of 5</span>

            </h4>

            {[

              "Offer letter signed",

              "Documents uploaded (ID, PAN, bank)",

              "Laptop & access provisioned",

              "Manager intro scheduled",

              "Policy acknowledgement",

            ].map((x, i) => (

              <div key={x} className={i < 3 ? "check done" : "check"}>

                <span>{i < 3 ? <Check size={13} /> : null}</span>

                {x}

              </div>

            ))}

          </div>

          <button className="link-btn">Send reminder</button>

        </section>

      </div>

    </Shell>

  );

}

function statusClass(s) {

  return s === "Active"

    ? "green"

    : s === "On leave"

      ? "amber"

      : s === "Onboarding"

        ? "blue"

        : "gray";

}



function Attendance({ setPage, darkMode, setDarkMode }) {

  const days = Array.from({ length: 30 }, (_, i) => i + 1);

  return (

    <Shell page="attendance" setPage={setPage} title="Attendance & Leave" darkMode={darkMode} setDarkMode={setDarkMode}>

      <div className="content">

        <PageHeader

          eyebrow="TIME OFF"

          title="Attendance & Leave"

          subtitle="Live check-ins, calendars and approvals"

          action="Apply leave"

        />

        <div className="stats-grid four">

          <Stat

            label="Checked in"

            value="2,209"

            note="as of 10:42 AM"

            tone="teal"

          />

          <Stat

            label="Late arrivals"

            value="64"

            note="+9 vs yesterday"

            tone="warning"

          />

          <Stat

            label="On leave today"

            value="118"

            note="12 unplanned"

            tone="amber"

          />

          <Stat

            label="Absent, no request"

            value="27"

            note="Follow up"

            tone="negative"

          />

        </div>

        <section className="card">

          <div className="card-head">

            <div>

              <h3>September 2026 · Team attendance</h3>

            </div>

            <div>

              <button className="icon-btn">

                <ChevronLeft size={16} />

              </button>

              <button className="icon-btn">

                <ChevronRight size={16} />

              </button>

            </div>

          </div>

          <div className="calendar">

            <div className="weekdays">

              {["M", "T", "W", "T", "F", "S", "S"].map((x, i) => (

                <b key={i}>{x}</b>

              ))}

            </div>

            <div className="days">

              {days.map((d) => (

                <div

                  key={d}

                  className={

                    "day " + (d % 7 === 0 || d % 7 === 6 ? "weekend" : "")

                  }

                >

                  <span>{d}</span>

                  {d < 23 && (

                    <i

                      className={

                        d % 9 === 0

                          ? "late"

                          : d % 11 === 0

                            ? "absent"

                            : "present"

                      }

                    />

                  )}

                </div>

              ))}

            </div>

          </div>

          <div className="legend">

            <span>

              <i className="present" />

              Present

            </span>

            <span>

              <i className="late" />

              Late / half day

            </span>

            <span>

              <i className="absent" />

              Absent

            </span>

            <span>

              <i className="leave" />

              Casual leave

            </span>

          </div>

        </section>

        <div className="leave-balance">

          {[

            ["Casual leave", "6 of 12 days left"],

            ["Sick leave", "9 of 10 days left"],

            ["Earned leave", "14 of 18 days left"],

          ].map((x) => (

            <div className="balance card" key={x[0]}>

              <small>{x[0]}</small>

              <strong>{x[1]}</strong>

              <div className="progress">

                <i

                  style={{

                    width:

                      x[0] === "Casual leave"

                        ? "50%"

                        : x[0] === "Sick leave"

                          ? "90%"

                          : "78%",

                  }}

                />

              </div>

            </div>

          ))}

        </div>

        <section className="card">

          <div className="card-head">

            <h3>Leave requests</h3>

            <span className="pill amber">Pending · 14</span>

          </div>

          <table>

            <thead>

              <tr>

                <th>Employee</th>

                <th>Type</th>

                <th>Dates</th>

                <th>Days</th>

                <th>Status</th>

                <th></th>

              </tr>

            </thead>

            <tbody>

              {[

                [

                  "AM",

                  "Arjun Mehta",

                  "Engineering",

                  "Casual",

                  "Oct 3 – 4",

                  "2",

                  "Pending",

                ],

                [

                  "FA",

                  "Farhan Ali",

                  "Engineering",

                  "Sick",

                  "Sep 25",

                  "1",

                  "Approved",

                ],

                [

                  "KR",

                  "Kavya Rao",

                  "Sales",

                  "Earned",

                  "Sep 22 – Oct 1",

                  "8",

                  "On leave",

                ],

                ["MN", "Meera Nair", "HR", "Casual", "Oct 10", "1", "Pending"],

              ].map((r) => (

                <tr key={r[1]}>

                  <td>

                    <div className="person">

                      <span className="avatar">{r[0]}</span>

                      <strong>{r[1]}</strong>

                    </div>

                  </td>

                  <td>{r[3]}</td>

                  <td>{r[4]}</td>

                  <td>{r[5]}</td>

                  <td>

                    <span

                      className={

                        "pill " +

                        (r[6] === "Pending"

                          ? "amber"

                          : r[6] === "Approved"

                            ? "green"

                            : "blue")

                      }

                    >

                      {r[6]}

                    </span>

                  </td>

                  <td>

                    {r[6] === "Pending" && (

                      <>

                        <button className="link-btn">Approve</button>

                        <button className="link-btn danger-text">Reject</button>

                      </>

                    )}

                  </td>

                </tr>

              ))}

            </tbody>

          </table>

        </section>

      </div>

    </Shell>

  );

}



function Payroll({ setPage, darkMode, setDarkMode }) {

  return (

    <Shell page="payroll" setPage={setPage} title="Payroll" darkMode={darkMode} setDarkMode={setDarkMode}>

      <div className="content">

        <PageHeader

          eyebrow="COMPENSATION"

          title="Payroll"

          subtitle="Automated salary runs, deductions and payslips"

          action="Approve run"

        />

        <section className="card payroll-run">

          <div>

            <small>SEPTEMBER 2026 PAYROLL RUN</small>

            <h3>Awaiting approval</h3>

          </div>

          <div className="steps">

            {["Prepare", "Validate", "Approve", "Disburse"].map((x, i) => (

              <div className={i < 2 ? "step done" : "step"} key={x}>

                <span>{i < 2 ? <Check size={13} /> : i + 1}</span>

                {x}

              </div>

            ))}

          </div>

        </section>

        <div className="stats-grid three">

          <Stat

            label="Gross payroll"

            value="₹14.82 Cr"

            note="2,386 employees"

            tone="teal"

          />

          <Stat

            label="Total deductions"

            value="₹2.71 Cr"

            note="PF, PT, TDS, ESI"

          />

          <Stat

            label="Net payable"

            value="₹12.11 Cr"

            note="+2.4% vs August"

            tone="positive"

          />

        </div>

        <section className="card">

          <div className="card-head">

            <h3>Exceptions to clear</h3>

            <span className="pill amber">4</span>

          </div>

          {[

            [

              "VS",

              "Vikram Singh",

              "Operations",

              "Final settlement pending (notice period)",

              "₹1,84,300",

              "Resolve",

            ],

            [

              "SK",

              "Sneha Kulkarni",

              "Finance",

              "Pro-rated salary · joined Sep 22",

              "₹21,450",

              "Review",

            ],

            [

              "RV",

              "Rahul Verma",

              "Production",

              "Overtime above policy cap (18h)",

              "₹6,120",

              "Review",

            ],

            [

              "AI",

              "Ananya Iyer",

              "Engineering",

              "Missing tax declaration (80C)",

              "TDS est.",

              "Remind",

            ],

          ].map((r) => (

            <div className="exception" key={r[1]}>

              <span className="avatar">{r[0]}</span>

              <div>

                <strong>{r[1]}</strong>

                <small>{r[2]}</small>

              </div>

              <p>{r[3]}</p>

              <b>{r[4]}</b>

              <button className="link-btn">{r[5]}</button>

            </div>

          ))}

        </section>

        <section className="card payslip">

          <div className="card-head">

            <h3>Payslip preview</h3>

            <button className="btn">PDF</button>

          </div>

          <div className="payslip-head">

            <span className="avatar large">AI</span>

            <div>

              <h3>Ananya Iyer</h3>

              <p className="mono">EMP-10421 · Sep 2026</p>

            </div>

          </div>

          <div className="salary">

            {[

              ["Basic", "₹48,000"],

              ["HRA", "₹19,200"],

              ["Special allowance", "₹14,800"],

              ["Provident fund", "− ₹5,760"],

              ["Professional tax", "− ₹200"],

              ["Income tax (TDS)", "− ₹6,940"],

            ].map((x) => (

              <div key={x[0]}>

                <span>{x[0]}</span>

                <b>{x[1]}</b>

              </div>

            ))}

            <div className="net">

              <span>Net pay</span>

              <b>₹69,100</b>

            </div>

          </div>

        </section>

      </div>

    </Shell>

  );

}



function Performance({ setPage, darkMode, setDarkMode }) {

  return (

    <Shell page="performance" setPage={setPage} title="Performance" darkMode={darkMode} setDarkMode={setDarkMode}>

      <div className="content">

        <PageHeader

          eyebrow="GROWTH"

          title="Performance"

          subtitle="Goals, reviews, 360° feedback and ratings"

          action="New review cycle"

        />

        <section className="card review-cycle">

          <div>

            <small>H2 2026 REVIEW CYCLE</small>

            <h3>Closes Oct 15</h3>

          </div>

          {[

            ["Self reviews", "82%"],

            ["Manager reviews", "47%"],

            ["360° feedback", "63%"],

            ["Calibrated", "12%"],

          ].map((x) => (

            <div key={x[0]}>

              <small>{x[0]}</small>

              <strong>{x[1]}</strong>

            </div>

          ))}

        </section>

        <div className="grid-2">

          <section className="card">

            <div className="card-head">

              <h3>Goals · Ananya Iyer</h3>

              <button className="btn">Switch employee</button>

            </div>

            {[

              ["Ship payroll v2 to 3 client orgs", "On track", "75%"],

              ["Reduce onboarding time to 5 days", "At risk", "40%"],

              ["Complete AWS certification", "On track", "90%"],

              ["Mentor 2 junior developers", "Behind", "20%"],

            ].map((x) => (

              <div className="goal" key={x[0]}>

                <div>

                  <strong>{x[0]}</strong>

                  <span

                    className={

                      "pill " +

                      (x[1] === "On track"

                        ? "green"

                        : x[1] === "At risk"

                          ? "amber"

                          : "red")

                    }

                  >

                    {x[1]}

                  </span>

                </div>

                <div className="progress">

                  <i style={{ width: x[2] }} />

                </div>

                <small>{x[2]}</small>

              </div>

            ))}

          </section>

          <section className="card">

            <div className="card-head">

              <h3>Rating distribution</h3>

            </div>

            {[

              ["1★", "3%"],

              ["2★", "9%"],

              ["3★", "41%"],

              ["4★", "34%"],

              ["5★", "13%"],

            ].map((x) => (

              <div className="bar-row" key={x[0]}>

                <span>{x[0]}</span>

                <div className="bar">

                  <i style={{ width: x[1] }} />

                </div>

                <b>{x[1]}</b>

              </div>

            ))}

          </section>

        </div>

        <section className="card">

          <div className="card-head">

            <h3>360° feedback summary</h3>

            <span>11 reviewers</span>

          </div>

          <div className="feedback-grid">

            {[

              ["Collaboration", "4.4"],

              ["Technical depth", "4.1"],

              ["Communication", "3.6"],

              ["Ownership", "4.3"],

            ].map((x) => (

              <div key={x[0]}>

                <small>{x[0]}</small>

                <strong>{x[1]}</strong>

                <div className="stars">★★★★★</div>

              </div>

            ))}

          </div>

        </section>

      </div>

    </Shell>

  );

}



function AIInsights({ setPage, darkMode, setDarkMode }) {

  return (

    <Shell page="ai" setPage={setPage} title="AI Insights" darkMode={darkMode} setDarkMode={setDarkMode}>

      <div className="content">

        <PageHeader

          eyebrow="INTELLIGENCE"

          title="AI Workforce Insights"

          subtitle="Attrition prediction, skill gaps and engagement"

          action="Ask NexusHR AI"

        />

        <div className="advisory">

          <BrainCircuit size={19} />

          <span>

            Predictions are advisory and based on attendance, performance, pay

            and engagement data. Review with context before acting.

          </span>

          <b>

            Model last trained Sep 20 · validation accuracy [YOUR MODEL

            ACCURACY]

          </b>

        </div>

        <section className="card">

          <div className="card-head">

            <h3>Attrition risk · top flagged</h3>

            <span className="pill teal">24 flagged</span>

          </div>

          {[

            [

              "AM",

              "Arjun Mehta",

              "Tech Lead · Engineering",

              "82",

              "No promotion 30 mo · overtime ↑",

            ],

            [

              "FA",

              "Farhan Ali",

              "QA Engineer · Engineering",

              "74",

              "Engagement ↓ 3 pulses in a row",

            ],

            [

              "KR",

              "Kavya Rao",

              "Account Executive · Sales",

              "68",

              "Pay below band by 12%",

            ],

            [

              "RV",

              "Rahul Verma",

              "Shift Supervisor · Production",

              "61",

              "Leave usage pattern change",

            ],

          ].map((x) => (

            <div className="risk" key={x[1]}>

              <span className="avatar">{x[0]}</span>

              <div>

                <strong>{x[1]}</strong>

                <small>{x[2]}</small>

              </div>

              <div className="risk-score">

                <b>{x[3]}</b>

                <small>Risk score</small>

              </div>

              <p>{x[4]}</p>

              <button className="btn">Plan action</button>

            </div>

          ))}

        </section>

        <div className="grid-2">

          <section className="card">

            <div className="card-head">

              <h3>Engagement score</h3>

              <strong>7.4 / 10</strong>

            </div>

            <div className="big-chart">

              <div className="chart-line" />

            </div>

            <div className="chart-axis">

              <span>Feb</span>

              <span>Sep</span>

            </div>

          </section>

          <section className="card">

            <div className="card-head">

              <h3>Skill gap matrix vs role requirements</h3>

            </div>

            <table className="matrix">

              <thead>

                <tr>

                  <th></th>

                  <th>Cloud</th>

                  <th>Java</th>

                  <th>React</th>

                  <th>Data</th>

                  <th>Security</th>

                </tr>

              </thead>

              <tbody>

                {[

                  [

                    "Engineering",

                    "Covered",

                    "High gap",

                    "Some gap",

                    "Covered",

                    "Some gap",

                  ],

                  [

                    "Operations",

                    "Covered",

                    "Covered",

                    "Covered",

                    "Some gap",

                    "Covered",

                  ],

                  [

                    "Finance",

                    "Covered",

                    "Covered",

                    "Covered",

                    "High gap",

                    "Some gap",

                  ],

                  [

                    "Sales",

                    "Covered",

                    "Covered",

                    "Covered",

                    "Some gap",

                    "Covered",

                  ],

                ].map((r) => (

                  <tr key={r[0]}>

                    <th>{r[0]}</th>

                    {r.slice(1).map((v, i) => (

                      <td key={i}>

                        <span

                          className={

                            v === "High gap"

                              ? "gap-high"

                              : v === "Some gap"

                                ? "gap-some"

                                : "gap-covered"

                          }

                        >

                          {v}

                        </span>

                      </td>

                    ))}

                  </tr>

                ))}

              </tbody>

            </table>

          </section>

        </div>

      </div>

    </Shell>

  );

}



function MobileHome({ setPage }) {

  return (

    <div className="mobile-screen">

      <div className="mobile-card">

        <div className="mobile-top">

          <Logo />

          <button className="icon-btn">

            <Bell size={17} />

          </button>

        </div>

        <div className="mobile-greeting">

          <small>AI</small>

          <h1>Hi, Ananya</h1>

          <p>Sunday, Sep 27 · General shift 9:30–6:30</p>

        </div>

        <section className="mobile-status">

          <div>

            <span>Not checked in</span>

            <small>Today</small>

          </div>

          <button className="btn primary">Check in</button>

        </section>

        <div className="leave-row">

          {[

            ["6", "Casual left"],

            ["9", "Sick left"],

            ["14", "Earned left"],

          ].map((x) => (

            <div key={x[1]}>

              <b>{x[0]}</b>

              <span>{x[1]}</span>

            </div>

          ))}

        </div>

        <section className="mobile-pay card">

          <small>SEPTEMBER PAYSLIP</small>

          <h2>₹69,100</h2>

          <p>Credited Sep 30</p>

          <button className="link-btn">

            View payslip <ArrowUpRight size={14} />

          </button>

        </section>

        <h3 className="mobile-section">Quick actions</h3>

        <div className="quick-grid">

          {[

            ["Apply leave", CalendarDays],

            ["Reimbursement", IndianRupee],

            ["My goals", Target],

            ["Documents", FileCheck2],

          ].map(([x, I]) => (

            <button key={x}>

              <I size={20} />

              <span>{x}</span>

            </button>

          ))}

        </div>

        <section className="mobile-goal card">

          <div className="card-head">

            <h3>Self review due Oct 15</h3>

            <span>3 of 5 sections complete</span>

          </div>

          <div className="progress">

            <i style={{ width: "60%" }} />

          </div>

        </section>

        <div className="mobile-nav">

          {[

            ["Home", Home],

            ["Leave", CalendarDays],

            ["Pay", WalletCards],

            ["Profile", UserRound],

          ].map(([x, I], i) => (

            <button className={i === 0 ? "active" : ""} key={x}>

              <I size={18} />

              <span>{x}</span>

            </button>

          ))}

        </div>

      </div>

    </div>

  );

}



function SignIn({ onEnter }) {

  return (

    <div className="signin">

      <div className="signin-left">

        <div className="signin-brand">

          <Logo />

        </div>

        <div className="signin-copy">

          <div className="eyebrow">NEXUSHR</div>

          <h1>

            Your whole workforce,

            <br />

            <em>one calm place</em> to run it.

          </h1>

          <p>

            Onboarding, attendance, payroll, performance and AI-driven workforce

            insights, with role-based access for admins, managers and employees.

          </p>

          <div className="feature-list">

            {[

              "Employee lifecycle",

              "Payroll Automated runs",

              "AI Insights Attrition & skills",

            ].map((x) => (

              <div key={x}>

                <Check size={15} />

                <span>{x}</span>

              </div>

            ))}

          </div>

        </div>

      </div>

      <div className="signin-panel">

        <div className="signin-form">

          <div className="mobile-logo">

            <Logo />

          </div>

          <div className="eyebrow">WELCOME BACK</div>

          <h2>Sign in</h2>

          <p>Use your work email to continue.</p>

          <label>

            Work email

            <input type="email" placeholder="you\@company.com" />

          </label>

          <label>

            Password

            <div className="input-wrap">

              <input type="password" placeholder="••••••••" />

              <button>Forgot password?</button>

            </div>

          </label>

          <label className="remember">

            <input type="checkbox" />

            Keep me signed in on this device

          </label>

          <button className="btn primary full" onClick={onEnter}>

            Sign in

          </button>

          <div className="or">

            <span>or explore without an account</span>

          </div>

          <div className="demo-buttons">

            <button onClick={onEnter}>Demo: Admin</button>

            <button onClick={onEnter}>Demo: Manager</button>

            <button onClick={onEnter}>Demo: Employee</button>

          </div>

        </div>

      </div>

    </div>

  );

}

function Reports({ setPage, darkMode, setDarkMode }) {
  return (
    <Shell
      page="reports"
      setPage={setPage}
      title="Reports"
      darkMode={darkMode}
      setDarkMode={setDarkMode}
    >
      <div className="content">

        <PageHeader
          eyebrow="ANALYTICS"
          title="Reports"
          subtitle="View and analyze your organization's HR reports"
        />

        <div className="stats-grid">

          <div className="stat-card">
            <div className="stat-label">Total Employees</div>
            <div className="stat-value">248</div>
            <div className="muted">Current workforce</div>
          </div>

          <div className="stat-card">
            <div className="stat-label">Attendance</div>
            <div className="stat-value">94%</div>
            <div className="muted">This month</div>
          </div>

          <div className="stat-card">
            <div className="stat-label">Leave Requests</div>
            <div className="stat-value">18</div>
            <div className="muted">Pending requests</div>
          </div>

          <div className="stat-card">
            <div className="stat-label">Payroll</div>
            <div className="stat-value">₹42.8L</div>
            <div className="muted">Monthly payroll</div>
          </div>

        </div>

        <div className="card">
          <div className="card-head">
            <div>
              <h3>Available Reports</h3>
              <p className="muted">
                Generate and view important HR reports.
              </p>
            </div>
          </div>

          <div className="settings-grid">

            <div className="setting-row">
              <div>
                <strong>Employee Report</strong>
                <p className="muted">
                  View employee information and workforce details.
                </p>
              </div>

              <button className="btn">
                View Report
              </button>
            </div>

            <div className="setting-row">
              <div>
                <strong>Attendance Report</strong>
                <p className="muted">
                  Review employee attendance and working hours.
                </p>
              </div>

              <button className="btn">
                View Report
              </button>
            </div>

            <div className="setting-row">
              <div>
                <strong>Leave Report</strong>
                <p className="muted">
                  Review leave requests and leave usage.
                </p>
              </div>

              <button className="btn">
                View Report
              </button>
            </div>

            <div className="setting-row">
              <div>
                <strong>Payroll Report</strong>
                <p className="muted">
                  Review salary and payroll information.
                </p>
              </div>

              <button className="btn">
                View Report
              </button>
            </div>

          </div>
        </div>

      </div>
    </Shell>
  );
}


function Setting({ setPage, darkMode, setDarkMode }) {

  return (

    <Shell
  page="settings"
  setPage={setPage}
  title="Settings"
  darkMode={darkMode}
  setDarkMode={setDarkMode}
>

      <div className="content">

        <PageHeader

          eyebrow="SYSTEM"

          title="Settings"

          subtitle="Manage your NexusHR account and application preferences"

        />



        <div className="settings-grid">

          {/* Account */}

          <section className="card settings-card">

            <div className="card-head">

              <div>

                <h3>Account</h3>

                <p className="muted">Manage your profile information.</p>

              </div>

            </div>



            <div className="setting-row">

              <div>

                <strong>Profile information</strong>

                <p className="muted">

                  Update your name and personal information.

                </p>

              </div>



              <button className="btn">Edit</button>

            </div>



            <div className="setting-row">

              <div>

                <strong>Password</strong>

                <p className="muted">Change your account password.</p>

              </div>



              <button className="btn">Change</button>

            </div>

          </section>



          {/* Notifications */} 

          <section className="card settings-card">

            <div className="card-head">

              <div>

                <h3>Notifications</h3>

                <p className="muted">Manage your notification preferences.</p>

              </div>

            </div>



            <div className="setting-row">

              <div>

                <strong>Email notifications</strong>

                <p className="muted">Receive important updates by email.</p>

              </div>



              <input type="checkbox" defaultChecked />

            </div>



            <div className="setting-row">

              <div>

                <strong>Leave notifications</strong>

                <p className="muted">Get notified about leave requests.</p>

              </div>



              <input type="checkbox" defaultChecked />

            </div>

          </section>



          {/* Language */}

          <section className="card settings-card">

            <div className="card-head">

              <div>

                <h3>Language</h3>

                <p className="muted">Select your preferred language.</p>

              </div>

            </div>



            <div className="setting-row">

              <div>

                <strong>Application language</strong>

              </div>



              <select className="btn">

                <option>English</option>

                <option>Hindi</option>

                <option>Odia</option>

              </select>

            </div>

          </section>

        </div>

      </div>

    </Shell>

  );

}



function App() {

  const [authed, setAuthed] = useState(false);

  const [page, setPage] = useState("dashboard");

  const [darkMode, setDarkMode] = useState(false);



  useEffect(() => {

    document.body.classList.toggle("dark-mode", darkMode);

  }, [darkMode]);



  if (!authed) {

    return <SignIn onEnter={() => setAuthed(true)} />;

  }



  if (page === "mobile") {

    return <MobileHome setPage={setPage} />;

  }



  const common = {

  setPage,

  darkMode,

  setDarkMode

};



  return page === "employees" ? (

    <Employees {...common} />

  ) : page === "attendance" ? (

    <Attendance {...common} />

  ) : page === "payroll" ? (

    <Payroll {...common} />

  ) : page === "performance" ? (

    <Performance {...common} />

  )  : page === "ai" ? (
  <AIInsights {...common} />
) : page === "reports" ? (
  <Reports {...common} />
) : page === "settings" ? (
  <Setting {...common} />

  ) : (

    <Dashboard {...common} />

  );

}

createRoot(document.getElementById("root")).render(<App />);
