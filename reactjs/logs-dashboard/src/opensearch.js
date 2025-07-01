const OPENSEARCH_URL = 'http://localhost:9200';
const AUTH = 'Basic ' + btoa('admin:SNTB@13nkt');

export async function fetchDashboardData() {
  try {
    const res = await fetch(`${OPENSEARCH_URL}/events-log/_search`, {
      method: 'POST',
      headers: {
        'Authorization': AUTH,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        size: 0,
        aggs: {
          by_target_type: {
            terms: { field: 'targetType.keyword', size: 10 }
          },
          by_subject_type: {
            terms: { field: 'subjectType.keyword', size: 10 }
          }
        }
      })
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(`OpenSearch error: ${data.error?.reason || res.statusText}`);
    }
    return data;
  } catch (err) {
    console.error('Error fetching dashboard data:', err);
    throw err;
  }
}

export async function fetchLogs(limit = 100) {
  try {
    const res = await fetch(`${OPENSEARCH_URL}/events-log/_search`, {
      method: 'POST',
      headers: {
        'Authorization': AUTH,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        size: limit,
        query: { match_all: {} },
        sort: [{ createdAt: { order: 'desc' } }]
      })
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(`OpenSearch error: ${data.error?.reason || res.statusText}`);
    }
    console.log('Fetched logs:', data); // Thêm log để debug
    return data;
  } catch (err) {
    console.error('Error fetching logs:', err);
    throw err;
  }
}

export async function searchLogsByTargetType(targetType) {
  try {
    const res = await fetch(`${OPENSEARCH_URL}/events-log/_search`, {
      method: 'POST',
      headers: {
        'Authorization': AUTH,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        size: 50,
        query: {
          match: {
            targetType: targetType
          }
        },
        sort: [{ createdAt: { order: 'desc' } }]
      })
    });
    const data = await res.json();
    if (!res.ok) {
      throw new Error(`OpenSearch error: ${data.error?.reason || res.statusText}`);
    }
    return data;
  } catch (err) {
    console.error('Error searching logs by targetType:', err);
    throw err;
  }
}