import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { Hotel, HotelFormValues, Room, RoomAvailability, RoomFormValues } from '@/types/hotel'

// catalog-service has no "my hotels" endpoint -- GET /api/hotels returns every
// hotel system-wide, so callers filter by ownerId client-side.
async function getAllHotels(): Promise<Hotel[]> {
  const { data } = await apiClient.get<Hotel[]>('/api/hotels')
  return data
}

async function createHotel(request: HotelFormValues): Promise<Hotel> {
  const { data } = await apiClient.post<Hotel>('/api/hotels', request)
  return data
}

async function updateHotel({ id, request }: { id: string; request: HotelFormValues }): Promise<Hotel> {
  const { data } = await apiClient.put<Hotel>(`/api/hotels/${id}`, request)
  return data
}

// Admin moderation -- gated to the ADMIN role server-side.
async function suspendHotel(id: string): Promise<Hotel> {
  const { data } = await apiClient.patch<Hotel>(`/api/hotels/${id}/suspend`)
  return data
}

async function reinstateHotel(id: string): Promise<Hotel> {
  const { data } = await apiClient.patch<Hotel>(`/api/hotels/${id}/reinstate`)
  return data
}

async function getRoomsForHotel(hotelId: string): Promise<Room[]> {
  const { data } = await apiClient.get<Room[]>(`/api/hotels/${hotelId}/rooms`)
  return data
}

async function createRoom({ hotelId, request }: { hotelId: string; request: RoomFormValues }): Promise<Room> {
  const { data } = await apiClient.post<Room>('/api/rooms', { ...request, hotelId })
  return data
}

async function updateRoom({ id, request }: { id: string; request: RoomFormValues }): Promise<Room> {
  const { data } = await apiClient.put<Room>(`/api/rooms/${id}`, request)
  return data
}

async function activateRoom(id: string): Promise<Room> {
  const { data } = await apiClient.patch<Room>(`/api/rooms/${id}/activate`)
  return data
}

async function deactivateRoom(id: string): Promise<Room> {
  const { data } = await apiClient.patch<Room>(`/api/rooms/${id}/deactivate`)
  return data
}

async function deleteRoom(id: string): Promise<Room> {
  const { data } = await apiClient.delete<Room>(`/api/rooms/${id}`)
  return data
}

export async function fetchRoomAvailability(
  roomId: string,
  startDate: string,
  endDate: string,
): Promise<RoomAvailability> {
  const { data } = await apiClient.get<RoomAvailability>(`/api/rooms/${roomId}/availability`, {
    params: { startDate, endDate },
  })
  return data
}

export const hotelKeys = {
  all: ['hotels'] as const,
  rooms: (hotelId: string) => ['hotels', hotelId, 'rooms'] as const,
}

export function useAllHotelsQuery() {
  return useQuery({ queryKey: hotelKeys.all, queryFn: getAllHotels })
}

export function useRoomsForHotelQuery(hotelId: string) {
  return useQuery({ queryKey: hotelKeys.rooms(hotelId), queryFn: () => getRoomsForHotel(hotelId) })
}

function useHotelMutation<TVars>(mutationFn: (vars: TVars) => Promise<Hotel>) {
  const queryClient = useQueryClient()
  return useMutation({ mutationFn, onSuccess: () => queryClient.invalidateQueries({ queryKey: hotelKeys.all }) })
}

// Room changes only touch that hotel's own room list.
function useRoomMutation<TVars>(hotelId: string, mutationFn: (vars: TVars) => Promise<Room>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: hotelKeys.rooms(hotelId) }),
  })
}

export const useCreateHotelMutation = () => useHotelMutation(createHotel)
export const useUpdateHotelMutation = () => useHotelMutation(updateHotel)
export const useSuspendHotelMutation = () => useHotelMutation(suspendHotel)
export const useReinstateHotelMutation = () => useHotelMutation(reinstateHotel)

export const useCreateRoomMutation = (hotelId: string) => useRoomMutation(hotelId, createRoom)
export const useUpdateRoomMutation = (hotelId: string) => useRoomMutation(hotelId, updateRoom)
export const useActivateRoomMutation = (hotelId: string) => useRoomMutation(hotelId, activateRoom)
export const useDeactivateRoomMutation = (hotelId: string) => useRoomMutation(hotelId, deactivateRoom)
export const useDeleteRoomMutation = (hotelId: string) => useRoomMutation(hotelId, deleteRoom)
