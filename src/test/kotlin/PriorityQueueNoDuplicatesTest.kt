import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import java.util.*

internal class PriorityQueueNoDuplicatesTest {

	lateinit var pq: PriorityQueue<Int>

	@BeforeEach
	fun setUp() {
		pq = PriorityQueueNoDuplicates()
	}

	//todo expand
	@Test
	fun offer() {
		pq.add(10)
		pq.add(20)
		pq.add(10)
		pq.size shouldBeEqualTo 2
	}


}