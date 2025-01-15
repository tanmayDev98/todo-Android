package com.example.todo.report

import android.content.Context
import android.net.Uri
import com.example.todo.R
import com.example.todo.repo.ToDoModel
import com.github.jknack.handlebars.Handlebars
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class RoasterListReport(
    private val context: Context,
    engine: Handlebars,
    private val appScope: CoroutineScope
) {
    private val template = engine.compileInline(context.getString(R.string.report_template))

    suspend fun generate(content: List<ToDoModel>, doc: Uri?) {
        withContext(Dispatchers.IO + appScope.coroutineContext) {
            if (doc != null) {
                context.contentResolver.openOutputStream(doc, "rwt")?.writer()?.use {
                    it.write(template.apply(content))
                    it.flush()
                }
            }
        }
    }
}