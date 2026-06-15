import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';

const COLORS = ['#E07A5F', '#8BA888', '#3D85C6', '#D4A373', '#9B72AA', '#6B5E50', '#C9ADA7'];

export function DiseaseBarChart({ diseases = [] }) {
  // Count diseases by type
  const counts = {};
  diseases.forEach(d => {
    const name = d.maladieDetectee || 'Inconnu';
    counts[name] = (counts[name] || 0) + 1;
  });
  const data = Object.entries(counts).map(([name, count]) => ({ name, count }));

  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#EDE7DA" />
          <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#6B5E50' }} />
          <YAxis tick={{ fontSize: 11, fill: '#6B5E50' }} allowDecimals={false} />
          <Tooltip contentStyle={{ backgroundColor: '#FBF8F3', border: '1px solid #EDE7DA', borderRadius: 8 }} />
          <Bar dataKey="count" fill="#E07A5F" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

export function DiseasePieChart({ diseases = [] }) {
  const counts = {};
  diseases.forEach(d => {
    const name = d.maladieDetectee || 'Inconnu';
    counts[name] = (counts[name] || 0) + 1;
  });
  const data = Object.entries(counts).map(([name, value]) => ({ name, value }));

  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
            {data.map((_, index) => (
              <Cell key={index} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Legend />
          <Tooltip />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
