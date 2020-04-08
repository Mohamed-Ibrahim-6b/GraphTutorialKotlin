package com.example.graphtutorial

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.microsoft.graph.models.extensions.DateTimeTimeZone
import com.microsoft.graph.models.extensions.Event
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class EventListAdapter(private val ctx: Context, private val resource: Int, events: List<Event>) :
    ArrayAdapter<Event>(ctx, resource, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        convertView?.apply {
            (tag as ViewHolder).bind(getItem(position))
        } ?: LayoutInflater.from(ctx).inflate(resource, parent, false).apply {
            ViewHolder(this).apply {
                tag = this
                bind(getItem(position))
            }
        }

    private class ViewHolder(val itemView: View) {

        private val localTimeZoneId: ZoneId = TimeZone.getDefault().toZoneId()

        fun bind(event: Event?) = event?.apply {
            itemView.findViewById<TextView>(R.id.eventsubject).text = subject
            itemView.findViewById<TextView>(R.id.eventorganizer).text = organizer.emailAddress.name
            itemView.findViewById<TextView>(R.id.eventstart).text = getLocalDateTimeString(start)
            itemView.findViewById<TextView>(R.id.eventend).text = getLocalDateTimeString(end)
        }

        // Convert Graph's DateTimeTimeZone format to
        // a LocalDateTime, then return a formatted string
        private fun getLocalDateTimeString(dateTime: DateTimeTimeZone): String {
            LocalDateTime.parse(dateTime.dateTime)
                .atZone(ZoneId.of(dateTime.timeZone))
                .withZoneSameInstant(localTimeZoneId).apply {
                    return String.format(
                        "%s %s",
                        format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                        format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                    )
                }
        }
    }
}