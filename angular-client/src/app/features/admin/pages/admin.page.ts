import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';
import { AppStateService } from '../../../core/services/app-state.service';
import { BookingStats } from '../../../core/models/booking.model';

@Component({
  selector: 'app-admin-page',
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './admin.page.html'
})
export class AdminPage implements OnInit {
  stats: BookingStats | null = null;

  fromDate = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10);
  toDate = new Date().toISOString().slice(0, 10);

  pieData: ChartData<'pie'> = { labels: [], datasets: [{ data: [] }] };
  barData: ChartData<'bar'> = { labels: [], datasets: [] };
  barMonthlyData: ChartData<'bar'> = { labels: [], datasets: [] };

  pieOptions: ChartOptions<'pie'> = { responsive: true };
  barOptions: ChartOptions<'bar'> = { responsive: true, plugins: { legend: { position: 'top' } } };

  constructor(private appState: AppStateService) {}

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.appState.getStats(
      `${this.fromDate}T00:00:00`,
      `${this.toDate}T23:59:59`,
      (stats) => {
        this.stats = stats;
        this.buildCharts(stats);
      }
    );
  }

  private buildCharts(stats: BookingStats) {
    this.pieData = {
      labels: Object.keys(stats.bookings_by_status),
      datasets: [{
        data: Object.values(stats.bookings_by_status).map(v => v ?? 0),
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444']
      }]
    };

    this.barData = {
      labels: stats.bookings_by_period.map(p => p.date),
      datasets: [
        {
          label: 'Revenue by Day',
          data: stats.bookings_by_period.map(p => Number(p.revenue)),
          backgroundColor: '#10b981'
        }
      ]
    };

    const monthlyRevenue = this.groupByMonth(stats.bookings_by_period);
    this.barMonthlyData = {
      labels: Array.from(monthlyRevenue.keys()),
      datasets: [
        {
          label: 'Revenue by Month',
          data: Array.from(monthlyRevenue.values()),
          backgroundColor: '#6366f1'
        }
      ]
    };
  }

  private groupByMonth(periods: { date: string; revenue: number }[]): Map<string, number> {
    const map = new Map<string, number>();
    for (const p of periods) {
      const month = p.date.slice(0, 7);
      map.set(month, (map.get(month) ?? 0) + Number(p.revenue));
    }
    return map;
  }
}
