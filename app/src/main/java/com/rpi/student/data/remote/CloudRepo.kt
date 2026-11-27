package com.rpi.student.data.remote

import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.services.Databases

/**
 * Chat hot_messages + groups + notices — Appwrite Databases।
 * v1 = text-only (Media Share flag OFF — blueprint §7)।
 * Torrent-style sealing (24h hot window → segments) backend cron phase-2 — hot_messages এখন hot window।
 */
object CloudRepo {

    private const val DB = "rpi_main"
    private const val MESSAGES = "hot_messages"
    private const val GROUPS = "groups"
    private const val NOTICES = "notices"

    data class ChatMessage(
        val id: String,
        val groupId: String,
        val userId: String,
        val userName: String,
        val body: String,
        val createdAt: Long, // epoch ms
    )

    data class ChatGroup(
        val id: String,
        val name: String,
        val kind: String,
        val nonLeaveable: Boolean,
        val memberCount: Int,
        val lastMessage: String,
    )

    private fun db(): Databases = Appwrite.service()

    private fun toMsg(doc: io.appwrite.models.Document<Map<String, Any>>): ChatMessage = ChatMessage(
        id = doc.id,
        groupId = doc.data["groupId"] as? String ?: "",
        userId = doc.data["userId"] as? String ?: "",
        userName = doc.data["userName"] as? String ?: "?",
        body = doc.data["body"] as? String ?: "",
        createdAt = parseTime(doc.createdAt),
    )

    private fun parseTime(iso: String): Long = runCatching {
        java.time.Instant.parse(iso).toEpochMilli()
    }.getOrElse { System.currentTimeMillis() }

    private fun toGroup(doc: io.appwrite.models.Document<Map<String, Any>>): ChatGroup = ChatGroup(
        id = doc.data["groupId"] as? String ?: doc.id,
        name = doc.data["name"] as? String ?: "Group",
        kind = doc.data["kind"] as? String ?: "CUSTOM",
        nonLeaveable = doc.data["nonLeaveable"] as? Boolean ?: true,
        memberCount = (doc.data["memberCount"] as? Number)?.toInt() ?: 0,
        lastMessage = doc.data["lastMessage"] as? String ?: "",
    )

    suspend fun groups(): Result<List<ChatGroup>> = runCatching {
        db().listDocuments(DB, GROUPS, listOf(Query.limit(50)))
            .documents
            .map { toGroup(it) }
    }

    suspend fun messages(groupId: String, myUserId: String): Result<List<ChatMessage>> = runCatching {
        db().listDocuments(
            DB,
            MESSAGES,
            listOf(
                Query.equal("groupId", groupId),
                Query.orderAsc("\$createdAt"),
                Query.limit(200),
            ),
        ).documents.map { toMsg(it) }
    }

    suspend fun sendMessage(groupId: String, myUserId: String, myName: String, body: String): Result<ChatMessage> = runCatching {
        val perms = listOf(
            Permission.read(Role.users()),
            Permission.update(Role.user(myUserId)),
            Permission.delete(Role.user(myUserId)),
        )
        val doc = db().createDocument(
            DB,
            MESSAGES,
            ID.unique(),
            mapOf(
                "groupId" to groupId,
                "userId" to myUserId,
                "userName" to myName,
                "body" to body,
            ),
            perms,
        )
        toMsg(doc)
    }

    data class Notice(
        val id: String,
        val title: String,
        val body: String,
        val technology: String,
        val createdAt: String,
    )

    suspend fun notices(): Result<List<Notice>> = runCatching {
        db().listDocuments(DB, NOTICES, listOf(Query.orderDesc("\$createdAt"), Query.limit(20)))
            .documents
            .map {
                Notice(
                    id = it.id,
                    title = it.data["title"] as? String ?: "",
                    body = it.data["body"] as? String ?: "",
                    technology = it.data["technology"] as? String ?: "All",
                    createdAt = it.createdAt,
                )
            }
    }
}
