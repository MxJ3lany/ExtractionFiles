/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.jobs;

import com.evernote.android.job.JobManager;

import es.usc.citius.servando.calendula.util.LogUtil;


public class CalendulaJobScheduler {

    private static final String TAG = "CalendulaJobScheduler";

    private CalendulaJobScheduler() {

    }

    public static void scheduleJob(CalendulaJob job) {
        // if there's already exactly one purge job running, leave it be
        int jobs = JobManager.instance().getAllJobsForTag(job.getTag()).size();
        int requests = JobManager.instance().getAllJobRequestsForTag(job.getTag()).size();

        LogUtil.d(TAG, "scheduleJob: There are " + jobs + " running and " + requests + " requests already for " + job.getTag());
        if (job.isUnique()) {
            if (jobs + requests != 1 || job.shouldOverwritePrevious()) {
                if (jobs + requests != 0) {
                    LogUtil.v(TAG, "Removing duplicate jobs/requests");
                    JobManager.instance().cancelAllForTag(job.getTag());
                }
                //and create one
                LogUtil.v(TAG, "Scheduling new job " + job.getTag());
                job.getRequest().schedule();
            }
        } else {
            LogUtil.v(TAG, "Scheduling new job " + job.getTag());
            job.getRequest().schedule();
        }

    }

    public static void scheduleJobs(CalendulaJob[] jobs) {
        for (CalendulaJob s : jobs) {
            scheduleJob(s);
        }
    }

}
