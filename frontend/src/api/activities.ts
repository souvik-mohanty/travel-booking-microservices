import { businessClient } from './client'
import type { Activity, ActivityFormValues } from '../types/activity'

export async function getActivity(id: string): Promise<Activity> {
  const { data } = await businessClient.get<Activity>(`/api/activities/${id}`)
  return data
}

export async function getActivitiesForBusiness(businessId: string): Promise<Activity[]> {
  const { data } = await businessClient.get<Activity[]>(`/api/businesses/${businessId}/activities`)
  return data
}

// Public search: only activities a tour is allowed to attach/book.
export async function getActiveActivities(): Promise<Activity[]> {
  const { data } = await businessClient.get<Activity[]>('/api/activities/active')
  return data
}

export async function createActivity(businessId: string, request: ActivityFormValues): Promise<Activity> {
  const { data } = await businessClient.post<Activity>('/api/activities', { ...request, businessId })
  return data
}

export async function updateActivity(id: string, request: ActivityFormValues): Promise<Activity> {
  const { data } = await businessClient.put<Activity>(`/api/activities/${id}`, request)
  return data
}

export async function activateActivity(id: string): Promise<Activity> {
  const { data } = await businessClient.patch<Activity>(`/api/activities/${id}/activate`)
  return data
}

export async function deactivateActivity(id: string): Promise<Activity> {
  const { data } = await businessClient.patch<Activity>(`/api/activities/${id}/deactivate`)
  return data
}

export async function deleteActivity(id: string): Promise<Activity> {
  const { data } = await businessClient.delete<Activity>(`/api/activities/${id}`)
  return data
}
