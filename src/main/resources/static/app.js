const form = document.getElementById('form');
const statusEl = document.getElementById('status');
const submitBtn = document.getElementById('submit');
const result = document.getElementById('result');
const output = document.getElementById('output');

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  statusEl.className = '';
  statusEl.textContent = 'Enhancing your resume... this can take up to a minute.';
  result.hidden = true;
  submitBtn.disabled = true;

  try {
    const res = await fetch('/api/enhance', { method: 'POST', body: new FormData(form) });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.error || 'Request failed (' + res.status + ')');
    output.textContent = data.enhanced; // textContent: never render model output as HTML
    result.hidden = false;
    statusEl.textContent = 'Done.';
  } catch (err) {
    statusEl.className = 'error';
    statusEl.textContent = err.message;
  } finally {
    submitBtn.disabled = false;
  }
});

document.getElementById('copy').addEventListener('click', async () => {
  await navigator.clipboard.writeText(output.textContent);
  statusEl.className = '';
  statusEl.textContent = 'Copied to clipboard.';
});
