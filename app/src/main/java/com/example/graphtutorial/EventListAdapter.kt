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

    private inner class ViewHolder {
        var subject: TextView? = null
        var organizer: TextView? = null
        var start: TextView? = null
        var end: TextView? = null
    }

    private val localTimeZoneId: ZoneId = TimeZone.getDefault().toZoneId()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val event = getItem(position)

        var holder = ViewHolder()

        val view: View = convertView?.apply {
            holder = tag as ViewHolder
        } ?: LayoutInflater.from(ctx).inflate(resource, parent, false).apply {
            holder.subject = findViewById(R.id.eventsubject)
            holder.organizer = findViewById(R.id.eventorganizer)
            holder.start = findViewById(R.id.eventstart)
            holder.end = findViewById(R.id.eventend)

            tag = holder
        }

        event!!.apply {
            holder.subject!!.text = subject
            holder.organizer!!.text = organizer.emailAddress.name
            holder.start!!.text = getLocalDateTimeString(start)
            holder.end!!.text = getLocalDateTimeString(end)
        }

        return view
    }

    // Convert Graph's DateTimeTimeZone format to
    // a LocalDateTime, then return a formatted string
    private fun getLocalDateTimeString(dateTime: DateTimeTimeZone): String {
        val localDateTime = LocalDateTime.parse(dateTime.dateTime)
            .atZone(ZoneId.of(dateTime.timeZone))
            .withZoneSameInstant(localTimeZoneId)

        return String.format(
            "%s %s",
            localDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
            localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        )
    }
}