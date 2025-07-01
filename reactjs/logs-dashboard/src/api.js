import axios from 'axios';

// Gọi trực tiếp OpenSearch - nhớ bật CORS như hướng dẫn trước
const BASE_URL = 'http://localhost:9200';
const AUTH = 'Basic ' + btoa('admin:SNTB@13nkt'); // Thay bằng user/pass của bạn

export const fetchAllLogs = async () => {
  const res = await axios.post(`${BASE_URL}/storage_event/_search`, {
    query: { match_all: {} },
    size: 100
  }, {
    headers: {
      'Authorization': AUTH,
      'Content-Type': 'application/json'
    }
  });

  return res.data.hits.hits.map(hit => hit._source);
};

export const fetchActionStats = async () => {
  const res = await axios.post(`${BASE_URL}/storage_event/_search`, {
    size: 0,
    aggs: {
      actions: {
        terms: {
          field: "action.keyword",
          size: 10
        }
      }
    }
  }, {
    headers: {
      'Authorization': AUTH,
      'Content-Type': 'application/json'
    }
  });

  return res.data.aggregations.actions.buckets;
};
