/*
 * 
 * Copyright (c) 2009 Tim Su
 * Copyright (c) 2010 Garen J. Torikian
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.miadzin.shelves.util.loan;

import java.text.Format;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.miadzin.shelves.util.Preferences;

public class DateControlSet implements OnTimeSetListener, OnDateSetListener,
		View.OnClickListener {

	private Format dateFormatter = null;
	private Format timeFormatter = null;

	protected final Activity activity;
	protected Button dateButton;
	protected Button timeButton;
	protected Date date;

	protected DateControlSet(Activity activity) {
		this.activity = activity;
		this.dateFormatter = Preferences.getDateFormat();
		this.timeFormatter = Preferences.getTimeFormat(this.activity);
	}

	public DateControlSet(Activity activity, Button dateButton,
			Button timeButton) {
		this(activity);

		this.dateButton = dateButton;
		this.timeButton = timeButton;
		dateButton.setOnClickListener(this);
		timeButton.setOnClickListener(this);

		setDate(null);
	}

	public DateControlSet(Activity activity, Button dateButton) {
		this(activity);

		this.dateButton = dateButton;
		dateButton.setOnClickListener(this);

		setDate(null);
	}

	public Date getDate() {
		return date;
	}

	/** Initialize the components for the given date field */
	public void setDate(Date newDate) {
		if (newDate == null) {
			date = new Date();
			date.setTime(date.getTime() + 1 * 24L * 3600 * 1000);
			date.setMinutes(0);
		} else
			this.date = new Date(newDate.getTime());

		updateDate();
		updateTime();
	}

	public void onDateSet(DatePicker view, int year, int month, int monthDay) {
		date.setYear(year - 1900);
		date.setMonth(month);
		date.setDate(monthDay);
		updateDate();
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		date.setHours(hourOfDay);
		date.setMinutes(minute);
		updateTime();
	}

	public void updateDate() {
		dateButton.setText(dateFormatter.format(date));
	}

	public void updateTime() {
		if (timeButton != null)
			timeButton.setText(timeFormatter.format(date));
	}

	public void onClick(View v) {
		if (v == timeButton)
			new TimePickerDialog(activity, this, date.getHours(),
					date.getMinutes(), Preferences.is24HourFormat(activity))
					.show();
		else
			new DatePickerDialog(activity, this, 1900 + date.getYear(),
					date.getMonth(), date.getDate()).show();
	}
}