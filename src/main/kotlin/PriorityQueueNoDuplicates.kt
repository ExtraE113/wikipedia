import java.util.*

class PriorityQueueNoDuplicates<E> : PriorityQueue<E>() {
	override fun offer(e: E): Boolean {
		var isAdded = false
		if (!super.contains(e)) {
			isAdded = super.offer(e)
		}
		return isAdded
	}
}