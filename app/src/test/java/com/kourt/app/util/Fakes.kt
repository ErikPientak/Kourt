package com.kourt.app.util

import com.kourt.app.data.model.Attendance
import com.kourt.app.data.model.Club
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.Rsvp
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.TeamStats
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AttendanceRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.ClubRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.RsvpRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamStatsRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.google.firebase.auth.FirebaseUser

/*
 * Hand-written fakes for the repository surface used by ViewModel unit tests.
 *
 * Each fake subclasses an `open` repository, but is itself instantiated through
 * [TestInstantiator.allocate] — which uses sun.misc.Unsafe to bypass *all*
 * constructors, including parent constructors that demand FirebaseAuth /
 * FirebaseFirestore. Because Unsafe also skips field initialisers, every
 * mutable field is initialised explicitly in the [create] factory rather than
 * relying on Kotlin property initialisers.
 *
 * Pattern: `var fooResult: ...` field per stubbable method; an optional
 * `failOn...` flag to simulate a thrown exception path for tests that need it.
 */

internal class FakeAuthRepository : AuthRepository(auth = TestInstantiator.allocate(com.google.firebase.auth.FirebaseAuth::class.java)) {

    var signInResult: Result<FirebaseUser> = Result.failure(NotImplementedError("signInResult not stubbed"))
    var resetResult: Result<Unit> = Result.success(Unit)
    var currentUserOverride: FirebaseUser? = null

    override val currentUser: FirebaseUser?
        get() = currentUserOverride

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = signInResult

    override suspend fun resetPassword(email: String): Result<Unit> = resetResult

    companion object {
        fun create(): FakeAuthRepository {
            val r = TestInstantiator.allocate(FakeAuthRepository::class.java)
            r.signInResult = Result.failure(NotImplementedError("signInResult not stubbed"))
            r.resetResult = Result.success(Unit)
            r.currentUserOverride = null
            return r
        }
    }
}

internal class FakeClubRepository : ClubRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var clubByAdmin: Club? = null
    var failOnGetClubByAdminId: Boolean = false

    override suspend fun getClubByAdminId(uid: String): Club? {
        if (failOnGetClubByAdminId) throw RuntimeException("fake getClubByAdminId failure")
        return clubByAdmin
    }

    companion object {
        fun create(): FakeClubRepository {
            val r = TestInstantiator.allocate(FakeClubRepository::class.java)
            r.clubByAdmin = null
            r.failOnGetClubByAdminId = false
            return r
        }
    }
}

internal class FakeTeamMemberRepository : TeamMemberRepository(
    firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java),
    rsvpRepository = TestInstantiator.allocate(RsvpRepository::class.java),
    playerStatsRepository = TestInstantiator.allocate(PlayerStatsRepository::class.java),
) {
    var membersByUser: List<TeamMember> = emptyList()
    var membersByTeam: List<TeamMember> = emptyList()
    var failOnGetMembersByUser: Boolean = false

    override suspend fun getMembersByUser(userId: String): List<TeamMember> {
        if (failOnGetMembersByUser) throw RuntimeException("fake getMembersByUser failure")
        return membersByUser
    }

    override suspend fun getMembersByTeam(teamId: String): List<TeamMember> = membersByTeam

    companion object {
        fun create(): FakeTeamMemberRepository {
            val r = TestInstantiator.allocate(FakeTeamMemberRepository::class.java)
            r.membersByUser = emptyList()
            r.membersByTeam = emptyList()
            r.failOnGetMembersByUser = false
            return r
        }
    }
}

internal class FakeUserRepository : UserRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var users: List<User> = emptyList()
    var userById: Map<String, User?> = emptyMap()

    override suspend fun getUser(id: String): User? = userById[id]

    override suspend fun getUsersByIds(ids: List<String>): List<User> {
        if (ids.isEmpty()) return emptyList()
        if (users.isEmpty()) return emptyList()
        val want = ids.toSet()
        return users.filter { it.id in want }
    }

    companion object {
        fun create(): FakeUserRepository {
            val r = TestInstantiator.allocate(FakeUserRepository::class.java)
            r.users = emptyList()
            r.userById = emptyMap()
            return r
        }
    }
}

internal class FakeEventRepository : EventRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var event: Event? = null
    var eventsByTeam: List<Event> = emptyList()
    var failOnGetEvent: Boolean = false

    override suspend fun getEvent(id: String): Event? {
        if (failOnGetEvent) throw RuntimeException("fake getEvent failure")
        return event
    }

    override suspend fun getEventsByTeam(teamId: String): List<Event> = eventsByTeam

    companion object {
        fun create(): FakeEventRepository {
            val r = TestInstantiator.allocate(FakeEventRepository::class.java)
            r.event = null
            r.eventsByTeam = emptyList()
            r.failOnGetEvent = false
            return r
        }
    }
}

internal class FakeAttendanceRepository : AttendanceRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    /**
     * Per-event override (used by trend tests to give different attendance lists
     * to different events). Falls back to [attendance] when the eventId is not
     * present in the map.
     */
    var attendanceByEvent: Map<String, List<Attendance>> = emptyMap()
    var attendance: List<Attendance> = emptyList()
    var savedAttendance: MutableList<Attendance> = mutableListOf()

    override suspend fun getAttendanceByEvent(eventId: String): List<Attendance> =
        attendanceByEvent[eventId] ?: attendance

    override suspend fun saveAttendance(attendance: Attendance): String {
        savedAttendance += attendance
        return "saved-${savedAttendance.size}"
    }

    companion object {
        fun create(): FakeAttendanceRepository {
            val r = TestInstantiator.allocate(FakeAttendanceRepository::class.java)
            r.attendanceByEvent = emptyMap()
            r.attendance = emptyList()
            r.savedAttendance = mutableListOf()
            return r
        }
    }
}

internal class FakeRsvpRepository : RsvpRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var rsvps: List<Rsvp> = emptyList()

    override suspend fun getRsvpsByEvent(eventId: String): List<Rsvp> = rsvps

    companion object {
        fun create(): FakeRsvpRepository {
            val r = TestInstantiator.allocate(FakeRsvpRepository::class.java)
            r.rsvps = emptyList()
            return r
        }
    }
}

internal class FakePlayerStatsRepository : PlayerStatsRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var playerStats: List<PlayerStats> = emptyList()
    var failOnGetPlayersStatsByTeam: Boolean = false

    override suspend fun getPlayerStats(teamMemberId: String, teamId: String): PlayerStats? =
        playerStats.firstOrNull { it.teamMemberId == teamMemberId && it.teamId == teamId }

    override suspend fun getPlayersStatsByTeam(teamId: String): List<PlayerStats> {
        if (failOnGetPlayersStatsByTeam) throw RuntimeException("fake getPlayersStatsByTeam failure")
        return playerStats
    }

    override suspend fun updatePlayerStats(stats: PlayerStats) { /* no-op */ }

    companion object {
        fun create(): FakePlayerStatsRepository {
            val r = TestInstantiator.allocate(FakePlayerStatsRepository::class.java)
            r.playerStats = emptyList()
            r.failOnGetPlayersStatsByTeam = false
            return r
        }
    }
}

internal class FakeTeamStatsRepository : TeamStatsRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var teamStats: TeamStats? = null

    override suspend fun getTeamStats(teamId: String): TeamStats? = teamStats
    override suspend fun updateTeamStats(stats: TeamStats) { /* no-op */ }

    companion object {
        fun create(): FakeTeamStatsRepository {
            val r = TestInstantiator.allocate(FakeTeamStatsRepository::class.java)
            r.teamStats = null
            return r
        }
    }
}

internal class FakeMatchStatsRepository : MatchStatsRepository(firestore = TestInstantiator.allocate(com.google.firebase.firestore.FirebaseFirestore::class.java)) {

    var matchStats: MatchStats? = null

    override suspend fun getMatchStats(eventId: String): MatchStats? = matchStats

    companion object {
        fun create(): FakeMatchStatsRepository {
            val r = TestInstantiator.allocate(FakeMatchStatsRepository::class.java)
            r.matchStats = null
            return r
        }
    }
}

internal class FakeAppPreferencesRepository : AppPreferencesRepository(context = TestInstantiator.allocate(android.content.Context::class.java)) {

    private var _activeTeamId: String = ""

    override var activeTeamId: String
        get() = _activeTeamId
        set(value) { _activeTeamId = value }

    companion object {
        fun create(): FakeAppPreferencesRepository {
            val r = TestInstantiator.allocate(FakeAppPreferencesRepository::class.java)
            // Touch property via reflection-free access by writing through the setter.
            r.activeTeamId = ""
            return r
        }
    }
}
