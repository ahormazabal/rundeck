package rundeck

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(Tag)
@Mock([ScheduledExecution, Workflow, CommandExec, Tag])
class TagSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test save with tags"() {
        given:
        def se = createBasicScheduledExecution()
        when:
        se.addToTags(Tag.getTag("TestTag1"))
        se.addToTags(Tag.getTag("TestTag2"))
        se.addToTags(Tag.getTag("TestTag3"))
        def result = se.save(flush: true, validate: true)

        then:
        result.getTags().size() == 3
        result.getTags().getAt(0).name == "testtag1"
        result.getTags().getAt(1).name == "testtag2"
        result.getTags().getAt(2).name == "testtag3"

    }

    void "test save without tags"() {
        given:
        def se = createBasicScheduledExecution()

        when:
        def result = se.save(true)

        then:
        result.getTags() == null
    }

    private static ScheduledExecution createBasicScheduledExecution() {
        new ScheduledExecution(
                jobName: "test",
                groupPath: "",
                description: "",
                project: "test",
                workflow: new Workflow(
                        commands: [
                                new CommandExec(adhocRemoteString: "exec")
                        ]
                ),
                options: [],
        )
    }

}
