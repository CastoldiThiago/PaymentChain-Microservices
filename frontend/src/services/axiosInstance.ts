import axios from 'axios';
import { keycloak } from '../auth/keycloak';

const instance = axios.create();

instance.interceptors.request.use(config => {
  if (keycloak.authenticated && keycloak.token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});

export default instance;
