import { useEffect, useMemo, useState } from 'react';
import { api } from '../api/client';

export default function Tasks({ token }) {
  const [tasks, setTasks] = useState([]); const [title, setTitle] = useState(''); const [description, setDescription] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(false);
  const load = async () => { try { setTasks(await api.listTasks(token)); } catch (err) { setError(err.message); } };
  useEffect(() => { load(); }, []);
  const create = async e => { e.preventDefault(); if (!title.trim()) return; setLoading(true); try { await api.createTask(token, title, description); setTitle(''); setDescription(''); await load(); } catch(err){setError(err.message)} finally{setLoading(false)} };
  const toggleDone = async task => { try { await api.updateTask(token, task.id, { title: task.title, description: task.description, status: task.status === 'DONE' ? 'TODO' : 'DONE' }); load(); } catch(err){setError(err.message)} };
  const remove = async id => { try { await api.deleteTask(token,id); load(); } catch(err){setError(err.message + ' (only ADMIN can delete)')} };
  const done = useMemo(() => tasks.filter(t => t.status === 'DONE'), [tasks]); const todo = useMemo(() => tasks.filter(t => t.status !== 'DONE'), [tasks]);
  return <div className="workspace-page">
    <div className="page-heading"><div><span className="eyebrow">WORKSPACE</span><h1>Task board</h1><p>Capture work, keep momentum, and ship what matters.</p></div><div className="task-count"><strong>{tasks.length}</strong><span>Total tasks</span></div></div>
    <div className="task-layout">
      <section className="create-task-card"><div className="section-icon">＋</div><h2>Create a task</h2><p>Turn the next thing on your list into an actionable task.</p><form onSubmit={create}><input placeholder="Task title" value={title} onChange={e=>setTitle(e.target.value)} required/><textarea rows="4" placeholder="What needs to be done?" value={description} onChange={e=>setDescription(e.target.value)}/><button className="primary-button" disabled={loading}>{loading ? 'Adding...' : 'Add task'} <span>→</span></button></form></section>
      <section className="board"><div className="board-col"><div className="col-head"><span><i className="dot pending"/>To do</span><b>{todo.length}</b></div>{todo.length===0 && <Empty text="Nothing pending. Nice!"/>}{todo.map(t=><TaskCard key={t.id} task={t} onToggle={toggleDone} onDelete={remove}/>)}</div><div className="board-col done-col"><div className="col-head"><span><i className="dot done"/>Completed</span><b>{done.length}</b></div>{done.length===0 && <Empty text="Completed tasks appear here."/>}{done.map(t=><TaskCard key={t.id} task={t} onToggle={toggleDone} onDelete={remove}/>)}</div></section>
    </div>{error && <div className="inline-error">{error}</div>}
  </div>;
}
function TaskCard({task,onToggle,onDelete}){return <article className={`task-card ${task.status==='DONE'?'completed':''}`}><div className="task-card-top"><span className="task-tag">TASK #{task.id}</span><button className="delete-lite" onClick={()=>onDelete(task.id)}>×</button></div><h3>{task.title}</h3><p>{task.description || 'No description added.'}</p><div className="task-card-foot"><button className="task-action" onClick={()=>onToggle(task)}>{task.status==='DONE'?'Reopen':'Mark done'} →</button><span>{task.status}</span></div></article>}
function Empty({text}){return <div className="empty-box">✓<span>{text}</span></div>}
