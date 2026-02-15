import type { SchoolListResponse } from '../types/nrwSchulkatalog';

export const nrwKatalogApi = {
  async getSchools(page: number = 0, pageSize: number = 50, sortBy: string = 'schulnummer', sortDir: string = 'asc'): Promise<SchoolListResponse> {
    const response = await fetch(
      `/api/nrw-schulkatalog/schools?page=${page}&pageSize=${pageSize}&sortBy=${sortBy}&sortDir=${sortDir}`
    );
    if (!response.ok) {
      throw new Error('Failed to fetch schools');
    }
    return response.json();
  },

  async searchSchools(query: string, page: number = 0, pageSize: number = 50, sortBy: string = 'schulnummer', sortDir: string = 'asc'): Promise<SchoolListResponse> {
    const response = await fetch(
      `/api/nrw-schulkatalog/search?q=${encodeURIComponent(query)}&page=${page}&pageSize=${pageSize}&sortBy=${sortBy}&sortDir=${sortDir}`
    );
    if (!response.ok) {
      throw new Error('Failed to search schools');
    }
    return response.json();
  },

  async refreshCatalog(): Promise<{ message: string }> {
    const response = await fetch('/api/nrw-schulkatalog/refresh', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (!response.ok) {
      throw new Error('Failed to refresh catalog');
    }
    return response.json();
  },
};
