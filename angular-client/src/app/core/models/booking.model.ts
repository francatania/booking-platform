export interface RescheduleRequest {
  start_time: string;
  end_time: string;
}

export interface BookingCreate {
  service_id: number;
  company_id: number;
  price: number;
  start_time: string;
  end_time: string;
  user_id?: number;
}

export interface BookingResponse {
  id: number;
  user_id: number;
  service_id: number;
  price: number;
  start_time: string;
  end_time: string;
  status: 'PENDING' | 'CANCELLED';
  created_at: string;
}
