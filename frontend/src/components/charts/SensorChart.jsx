import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';

const typeColors = {
  'Temperature': '#E07A5F',
  'Humidite': '#3D85C6',
  'pH': '#8BA888',
};

export default function SensorChart({ data = [], type = 'Temperature', min, max, unite = '' }) {
  const color = typeColors[type] || '#8BA888';

  const formatted = data.map(d => ({
    ...d,
    time: d.dateReleve ? new Date(d.dateReleve).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }) : '',
  })).reverse();

  return (
    <div className="h-64">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={formatted} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#EDE7DA" />
          <XAxis dataKey="time" tick={{ fontSize: 11, fill: '#6B5E50' }} />
          <YAxis tick={{ fontSize: 11, fill: '#6B5E50' }} unit={` ${unite}`} />
          <Tooltip
            contentStyle={{ backgroundColor: '#FBF8F3', border: '1px solid #EDE7DA', borderRadius: 8 }}
            formatter={(value) => [`${value} ${unite}`, type]}
          />
          {min != null && <ReferenceLine y={min} stroke="#E07A5F" strokeDasharray="5 5" label={{ value: `Min: ${min}`, fill: '#E07A5F', fontSize: 10 }} />}
          {max != null && <ReferenceLine y={max} stroke="#E07A5F" strokeDasharray="5 5" label={{ value: `Max: ${max}`, fill: '#E07A5F', fontSize: 10 }} />}
          <Line type="monotone" dataKey="valeur" stroke={color} strokeWidth={2} dot={false} activeDot={{ r: 4 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
