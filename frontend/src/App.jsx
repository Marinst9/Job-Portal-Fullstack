import React, { useState, useEffect } from 'react';
import { Upload, Loader2, CheckCircle2, Briefcase, PlusCircle, LogOut, LayoutDashboard, Search, MapPin, AlertCircle } from 'lucide-react';

const App = () => {
  // --- States ---
  const [jobs, setJobs] = useState([]);
  const [cvText, setCvText] = useState("");
  const [scanning, setScanning] = useState(false);
  const [matches, setMatches] = useState({});
  const [searchTerm, setSearchTerm] = useState("");
  
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('loggedUser')) || null);
  const [view, setView] = useState('landing'); 
  const [authMode, setAuthMode] = useState(null); 
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isPostJobOpen, setIsPostJobOpen] = useState(false);
  const [selectedJob, setSelectedJob] = useState(null);
  
  const [authData, setAuthData] = useState({ fullName: '', email: '', password: '', role: 'CANDIDATE' });
  const [newJob, setNewJob] = useState({ title: '', companyName: '', location: '', salary: '', description: '' });
  const [employerApplications, setEmployerApplications] = useState([]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState(false);

  // --- Ефекти ---
  useEffect(() => { 
    loadJobs(); 
    if (user) {
      if (user.role?.toUpperCase() === 'EMPLOYER') {
        loadEmployerData();
      } else if (view === 'dashboard') {
        setView('landing'); 
      }
    }
  }, [user]);

  const loadJobs = () => {
    fetch('http://localhost:8081/api/v1/jobs')
      .then(res => res.json())
      .then(data => setJobs(data))
      .catch(() => setJobs([]));
  };

  const loadEmployerData = async () => {
    try {
      const res = await fetch('http://localhost:8081/api/v1/applications');
      const data = await res.json();
      setEmployerApplications(data);
    } catch (e) { console.error("Грешка при вчитување апликации", e); }
  };

  const handleAuth = async (e) => {
    e.preventDefault();
    const endpoint = authMode === 'login' ? 'login' : 'register';
    try {
      const res = await fetch(`http://localhost:8081/api/v1/auth/${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(authData)
      });
      const data = await res.json();
      if (res.ok) {
        localStorage.setItem('loggedUser', JSON.stringify(data));
        setUser(data);
        setAuthMode(null);
        setView(data.role?.toUpperCase() === 'EMPLOYER' ? 'dashboard' : 'landing');
      } else {
        alert(data.error || "Грешка при најава!");
      }
    } catch (error) {
      alert("Серверот не е достапен.");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('loggedUser');
    setUser(null);
    setView('landing');
    setMatches({});
  };

  const handlePostJob = async (e) => {
    e.preventDefault();
    const jobToSave = { ...newJob, employer: { id: user.id } };
    const res = await fetch('http://localhost:8081/api/v1/jobs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(jobToSave)
    });
    if (res.ok) {
      setIsPostJobOpen(false);
      loadJobs();
    }
  };

  const handleSubmitApplication = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    const payload = {
      job: { id: selectedJob.id },
      user: { id: user.id },
      coverLetter: cvText,
      status: "PENDING"
    };

    try {
        const res = await fetch('http://localhost:8081/api/v1/applications', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
      
          if (res.ok) {
            setSuccessMessage(true);
            if (user?.role?.toUpperCase() === 'EMPLOYER') loadEmployerData();
            setTimeout(() => { setIsModalOpen(false); setSuccessMessage(false); }, 2000);
          }
    } catch (e) { alert("Грешка при праќање."); }
    setIsSubmitting(false);
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = async (event) => {
      const text = event.target.result;
      setCvText(text);
      setScanning(true);
      try {
        const res = await fetch('http://localhost:8081/api/v1/jobs/match', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ cvText: text })
        });
        const data = await res.json();
        setMatches(data);
      } catch (e) { console.error(e); }
      setScanning(false);
    };
    reader.readAsText(file);
  };

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-900">
      {/* --- HEADER --- */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-sm">
        <div className="max-w-6xl mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center gap-2 cursor-pointer" onClick={() => setView('landing')}>
            <div className="bg-blue-600 p-2 rounded-lg shadow-lg shadow-blue-200">
                <Briefcase className="text-white" size={24} />
            </div>
            <h1 className="text-2xl font-black tracking-tighter uppercase italic">MarinaJobs<span className="text-blue-600 not-italic">.AI</span></h1>
          </div>
          
          <div className="flex items-center gap-4">
            {!user ? (
              <div className="flex gap-2">
                <button onClick={() => setAuthMode('login')} className="text-sm font-bold text-slate-600 hover:text-blue-600 px-2 transition-colors">Најава</button>
                <button onClick={() => setAuthMode('signup')} className="bg-slate-900 text-white px-5 py-2.5 rounded-full font-bold text-sm hover:bg-slate-800 transition-all">Регистрација</button>
              </div>
            ) : (
              <div className="flex items-center gap-4">
                {user.role?.toUpperCase() === 'EMPLOYER' && (
                  <button onClick={() => setView(view === 'dashboard' ? 'landing' : 'dashboard')} className="flex items-center gap-2 text-sm font-bold text-blue-600 bg-blue-50 px-4 py-2 rounded-xl border border-blue-100 hover:bg-blue-100 transition-all">
                    <LayoutDashboard size={18}/> {view === 'dashboard' ? 'Види Огласи' : 'Админ Панел'}
                  </button>
                )}
                <div className="flex items-center gap-3 border-l pl-4 border-slate-200">
                  <div className="text-right">
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">{user.role}</p>
                    <p className="text-sm font-bold leading-tight">{user.fullName}</p>
                  </div>
                  <button onClick={handleLogout} className="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"><LogOut size={20}/></button>
                </div>
              </div>
            )}
            
            {view === 'landing' && (
                <label className="ml-2 flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white rounded-full cursor-pointer hover:bg-blue-700 hover:scale-105 transition-all shadow-md active:scale-95">
                    {scanning ? <Loader2 className="animate-spin" size={18} /> : <Upload size={18} />}
                    <span className="text-xs font-black uppercase tracking-wider">{cvText ? "CV Скенирано" : "Скенирај CV"}</span>
                    <input type="file" className="hidden" accept=".txt" onChange={handleFileUpload} />
                </label>
            )}
          </div>
        </div>
      </header>

      {/* --- ГЛАВНА СОДРЖИНА --- */}
      {view === 'dashboard' && user?.role?.toUpperCase() === 'EMPLOYER' ? (
        <main className="max-w-6xl mx-auto p-10 animate-in fade-in slide-in-from-bottom-4 duration-500">
          <div className="flex justify-between items-end mb-10">
            <div>
              <h2 className="text-4xl font-black italic uppercase tracking-tighter">Менаџирај <span className="text-blue-600">Апликации</span></h2>
              <p className="text-slate-500 font-medium">Преглед на кандидати рангирани според AI Match Score.</p>
            </div>
            <button onClick={() => setIsPostJobOpen(true)} className="bg-slate-900 text-white px-8 py-4 rounded-2xl font-bold flex items-center gap-2 hover:bg-blue-600 transition-all shadow-xl shadow-slate-200 active:scale-95">
              <PlusCircle size={20}/> Објави нов оглас
            </button>
          </div>

          <div className="bg-white rounded-[2.5rem] border border-slate-200 overflow-hidden shadow-2xl shadow-slate-100">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/50 border-b border-slate-100">
                  <th className="p-8 text-[11px] uppercase font-black text-slate-400 tracking-widest">Кандидат</th>
                  <th className="p-8 text-[11px] uppercase font-black text-slate-400 tracking-widest">Позиција</th>
                  <th className="p-8 text-[11px] uppercase font-black text-slate-400 tracking-widest text-center">AI Match %</th>
                  <th className="p-8 text-[11px] uppercase font-black text-slate-400 tracking-widest text-right">Документ</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {employerApplications.map(app => (
                  <tr key={app.id} className="hover:bg-blue-50/30 transition-colors group">
                    <td className="p-8">
                      <div className="font-bold text-slate-900 text-lg">{app.user?.fullName}</div>
                      <div className="text-sm text-slate-400 font-medium">{app.user?.email}</div>
                    </td>
                    <td className="p-8">
                      <span className="inline-block px-3 py-1 bg-slate-100 text-slate-600 rounded-lg text-xs font-bold uppercase tracking-tighter">{app.job?.title}</span>
                    </td>
                    <td className="p-8 text-center">
                      <div className={`inline-flex items-center justify-center w-14 h-14 rounded-2xl text-lg font-black shadow-inner ${
                          app.aiMatchScore >= 75 ? 'bg-green-100 text-green-600' : 
                          app.aiMatchScore >= 45 ? 'bg-yellow-100 text-yellow-600' : 'bg-red-100 text-red-600'
                        }`}>
                        {app.aiMatchScore || 0}%
                      </div>
                    </td>
                    <td className="p-8 text-right">
                      <button onClick={() => {setSelectedJob(app.job); setCvText(app.coverLetter); setIsModalOpen(true);}} className="bg-white border border-slate-200 px-4 py-2 rounded-xl text-xs font-black uppercase text-slate-600 hover:border-blue-600 hover:text-blue-600 transition-all group-hover:shadow-md">Види CV</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </main>
      ) : (
        <>
          <section className="bg-slate-900 text-white py-24 px-6 text-center relative overflow-hidden">
             <div className="absolute top-0 left-0 w-full h-full opacity-20 pointer-events-none bg-[radial-gradient(circle_at_center,_#3b82f6_0%,_transparent_70%)]"></div>
             <h2 className="text-7xl font-black mb-8 leading-tight italic tracking-tighter animate-in zoom-in-95 duration-700">Твојата кариера <br/>заслужува <span className="text-blue-500">AI моќ</span></h2>
             <div className="relative max-w-2xl mx-auto z-10 group">
                <Search className="absolute left-6 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-500 transition-colors" size={24} />
                <input 
                  type="text" 
                  placeholder="Пребарај ја твојата следна омилена работа..." 
                  className="w-full pl-16 pr-8 py-6 bg-white rounded-[2rem] text-slate-900 shadow-2xl outline-none text-xl font-medium focus:ring-4 ring-blue-500/20 transition-all"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
             </div>
          </section>

          <main className="max-w-6xl mx-auto p-6 -mt-12 grid grid-cols-1 md:grid-cols-2 gap-8 relative z-20 mb-20">
            {jobs.filter(j => j.title.toLowerCase().includes(searchTerm.toLowerCase())).map(job => (
              <div key={job.id} className="bg-white p-10 rounded-[3rem] border border-slate-200 hover:shadow-2xl hover:border-blue-200 transition-all relative group overflow-hidden">
                {matches[job.id] && (
                  <div className="absolute top-0 right-0 bg-green-500 text-white px-6 py-2 rounded-bl-3xl text-sm font-black shadow-lg animate-pulse">
                    AI MATCH: {matches[job.id]}%
                  </div>
                )}
                <h3 className="text-3xl font-black mb-2 group-hover:text-blue-600 transition-colors">{job.title}</h3>
                <p className="text-blue-600 font-bold text-sm uppercase mb-8 tracking-[0.2em]">{job.companyName}</p>
                <div className="flex flex-wrap gap-4 mb-10 text-slate-500 text-sm font-bold">
                  <span className="flex items-center gap-2 bg-slate-50 px-4 py-2 rounded-xl border border-slate-100"><MapPin size={18} className="text-blue-500"/> {job.location}</span>
                  <span className="flex items-center gap-2 bg-slate-50 px-4 py-2 rounded-xl border border-slate-100 text-slate-900">€ {job.salary}</span>
                </div>
                <button 
                  onClick={() => { setSelectedJob(job); setIsModalOpen(true); }}
                  className="w-full py-5 bg-slate-900 text-white rounded-[1.5rem] font-black text-sm uppercase tracking-widest hover:bg-blue-600 transition-all shadow-xl active:scale-95"
                >
                  Аплицирај веднаш
                </button>
              </div>
            ))}
          </main>
        </>
      )}

      {/* --- МОДАЛИ --- */}

      {/* Auth Modal */}
      {authMode && (
        <div className="fixed inset-0 bg-slate-900/95 backdrop-blur-md flex items-center justify-center z-[100] p-4">
          <form onSubmit={handleAuth} className="bg-white p-12 rounded-[3rem] w-full max-w-md space-y-5 shadow-2xl animate-in zoom-in-90 duration-300">
            {/* ПОПРАВЕН НАСЛОВ БЕЗ <BR/> */}
            <h2 className="text-4xl font-black italic text-center uppercase tracking-tighter mb-8 leading-tight">
                {authMode === 'login' ? (
                  <>Добредојде <span className="block text-blue-600">назад</span></>
                ) : (
                  <>Креирај <span className="block text-blue-600">профил</span></>
                )}
            </h2>

            {authMode === 'signup' && (
              <input required placeholder="Целосно име" className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 focus:border-blue-500 outline-none font-medium" onChange={e => setAuthData({...authData, fullName: e.target.value})} />
            )}
            <input required type="email" placeholder="Email адреса" className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 focus:border-blue-500 outline-none font-medium" onChange={e => setAuthData({...authData, email: e.target.value})} />
            <input required type="password" placeholder="Лозинка" className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 focus:border-blue-500 outline-none font-medium" onChange={e => setAuthData({...authData, password: e.target.value})} />
            {authMode === 'signup' && (
              <select className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 font-bold text-slate-600" onChange={e => setAuthData({...authData, role: e.target.value})}>
                <option value="CANDIDATE">Барам работа (Кандидат)</option>
                <option value="EMPLOYER">Нудам работа (Работодавач)</option>
              </select>
            )}
            <button type="submit" className="w-full py-5 bg-blue-600 text-white rounded-2xl font-black uppercase text-sm tracking-widest hover:bg-blue-700 transition-all shadow-xl shadow-blue-200 active:scale-95">Влези во апликацијата</button>
            <button type="button" onClick={() => setAuthMode(null)} className="w-full text-xs font-black text-slate-400 uppercase text-center tracking-widest hover:text-slate-600">Откажи</button>
          </form>
        </div>
      )}

      {/* Modal za Apliciranje / Najava */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-slate-900/90 backdrop-blur-sm flex items-center justify-center z-[100] p-4">
          <div className="bg-white p-10 rounded-[2.5rem] w-full max-w-sm relative shadow-2xl">
            
            {!user ? (
              <div className="text-center space-y-6">
                <div className="bg-red-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto text-red-500 shadow-inner">
                  <AlertCircle size={40} />
                </div>
                <div>
                  <h2 className="text-2xl font-black italic uppercase mb-2 leading-none">Стоп!</h2>
                  <p className="text-slate-500 font-medium text-sm">Мора да се најавите за да испратите апликација.</p>
                </div>
                <div className="flex flex-col gap-3">
                  <button onClick={() => { setAuthMode('login'); setIsModalOpen(false); }} className="w-full py-4 bg-slate-900 text-white rounded-2xl font-black uppercase text-xs tracking-widest hover:bg-blue-600 transition-all">Најави се</button>
                  <button onClick={() => { setAuthMode('signup'); setIsModalOpen(false); }} className="w-full py-4 bg-slate-100 text-slate-900 rounded-2xl font-black uppercase text-xs tracking-widest hover:bg-slate-200 transition-all">Регистрирај се</button>
                </div>
                <button type="button" onClick={() => setIsModalOpen(false)} className="text-xs font-bold text-slate-400 uppercase tracking-widest">Затвори</button>
              </div>
            ) : successMessage ? (
              <div className="text-center py-10">
                <div className="w-24 h-24 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-6 text-green-500">
                   <CheckCircle2 size={60} className="animate-bounce" />
                </div>
                <h2 className="text-2xl font-black italic uppercase">Успешно!</h2>
                <p className="text-slate-500 text-sm font-medium">Вашата апликација е веќе во рацете на работодавачот.</p>
              </div>
            ) : (
              <form onSubmit={handleSubmitApplication} className="space-y-6">
                <div className="border-b pb-4">
                  <p className="text-[10px] font-black text-blue-600 uppercase tracking-widest mb-1">Аплицираш за:</p>
                  <h2 className="text-2xl font-black italic uppercase leading-none">{selectedJob?.title}</h2>
                </div>
                
                <div className="p-4 bg-blue-50 rounded-2xl border border-blue-100 flex gap-3 items-start shadow-inner">
                  <input type="checkbox" required className="mt-1 w-5 h-5 accent-blue-600 rounded cursor-pointer" />
                  <p className="text-[10px] font-bold text-blue-800 leading-tight uppercase">Се согласувам со условите за обработка на моите податоци преку AI анализа.</p>
                </div>

                <div className={`p-4 rounded-2xl border-2 border-dashed transition-all ${cvText ? 'bg-green-50 border-green-200 text-green-700' : 'bg-red-50 border-red-200 text-red-600'}`}>
                   <p className="text-[10px] uppercase font-black opacity-60 mb-1">Статус на твојот документ</p>
                   {cvText ? (
                      <div className="flex items-center gap-2 text-xs font-black">
                         <CheckCircle2 size={16} /> CV-то е подготвено
                      </div>
                   ) : (
                      <p className="text-xs font-black italic uppercase tracking-tighter">Мораш прво да скенираш CV!</p>
                   )}
                </div>

                <button 
                  disabled={!cvText || isSubmitting} 
                  className="w-full py-5 bg-blue-600 text-white rounded-2xl font-black uppercase tracking-widest disabled:bg-slate-200 disabled:text-slate-400 shadow-xl shadow-blue-100 active:scale-95 transition-all"
                >
                  {isSubmitting ? "Процесирање..." : "Испрати апликација"}
                </button>
                <button type="button" onClick={() => setIsModalOpen(false)} className="w-full text-xs font-black text-slate-400 text-center uppercase tracking-widest">Откажи</button>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Modal za Postiranje Oglas */}
      {isPostJobOpen && (
        <div className="fixed inset-0 bg-slate-900/90 backdrop-blur-md flex items-center justify-center z-[100] p-4">
          <form onSubmit={handlePostJob} className="bg-white p-12 rounded-[3.5rem] w-full max-w-lg space-y-4 shadow-2xl relative animate-in slide-in-from-bottom-10">
            <h2 className="text-3xl font-black italic uppercase tracking-tighter mb-4">Објави <span className="text-blue-600">Оглас</span></h2>
            <input required placeholder="Наслов (пр. Senior React Developer)" className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 outline-none focus:border-blue-500 font-bold" onChange={e => setNewJob({...newJob, title: e.target.value})} />
            <input required placeholder="Компанија (пр. Marina Tech)" className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 outline-none" onChange={e => setNewJob({...newJob, companyName: e.target.value})} />
            <div className="grid grid-cols-2 gap-4">
              <input required placeholder="Локација" className="p-5 bg-slate-50 rounded-2xl border border-slate-200" onChange={e => setNewJob({...newJob, location: e.target.value})} />
              <input required type="number" placeholder="Плата (€)" className="p-5 bg-slate-50 rounded-2xl border border-slate-200 font-bold" onChange={e => setNewJob({...newJob, salary: e.target.value})} />
            </div>
            <textarea required placeholder="Опис на позицијата и барани вештини (ова е важно за AI Match)..." className="w-full p-5 bg-slate-50 rounded-2xl border border-slate-200 h-40 outline-none focus:border-blue-500" onChange={e => setNewJob({...newJob, description: e.target.value})} />
            <button type="submit" className="w-full py-5 bg-slate-900 text-white rounded-2xl font-black uppercase tracking-widest hover:bg-blue-600 transition-all shadow-xl active:scale-95">Креирај оглас</button>
            <button type="button" onClick={() => setIsPostJobOpen(false)} className="w-full text-xs font-black text-slate-400 text-center uppercase tracking-widest">Затвори</button>
          </form>
        </div>
      )}
    </div>
  );
};

export default App;