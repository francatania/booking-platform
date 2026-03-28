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

export interface BookingPeriodStat {
  date: string;
  count: number;
  revenue: number;
}

export interface BookingStats {
  total_bookings: number;
  bookings_by_status: { [status: string]: number | undefined };
  total_revenue: number;
  bookings_by_period: BookingPeriodStat[];
}

export interface BookingDetailResponse {
  id: number;
  user_id: number;
  service_id: number;
  company_id: number;
  user_username: string;
  user_first_name: string;
  user_last_name: string;
  service_name: string;
  start_time: string;
  end_time: string;
  price: number;
  status: string;
  created_at: string;
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
