package rundeck

import java.util.stream.Collectors


/**
 * Domain class to represent a searchable tag string.
 */
class Tag {
    String name

    static belongsTo = [ScheduledExecution]
    static hasMany = [jobs: ScheduledExecution]
    static mapping = {
        table "tags"
    }

    void setName(String name) {
        this.name = name.toLowerCase()
    }

    @Override
    String toString() {
        return name
    }

    /**
     * Gets an existing tag instance from db or creates a new one if not exists.
     * @param tagName Name of the tag (case insensitive)
     * @return Tag instance object.
     */
    static Tag getTag(String tagName) {
        def tname = tagName.toLowerCase()
        return Tag.findByName(tname) ?: new Tag(name: tname)
    }

    /**
     * Converts a tag set into a comma separated string.
     * @param tagList
     * @return
     */
    static String asString(Set<Tag> tagList) {
        return tagList == null ? null :
                tagList.stream()
                        .map { it.name }
                        .collect(Collectors.joining(",")) ?: null
    }

    /**
     * Converts a comma-separated tag list string into a set of tag objects.
     * @param tagListString
     * @return a Set of tag objects
     */
    static Set<Tag> asSet(String tagListString) {
        if (!tagListString || tagListString.trim().isEmpty())
            return null

        return Arrays.stream(tagListString.split(","))
                .map { it.trim() }
                .filter { !it.isEmpty() }
                .map { Tag.getTag(it) }
                .collect(Collectors.toSet())
    }

    static List<Object[]> getTagsAndCountByProject(String projectName) {
        if(!projectName || projectName.trim().isEmpty())
            return null

        //having jobs having projects with the name

        def c = Tag.createCriteria()
        def tagsByProject = c.list {
            projections{
                groupProperty("name")
                count("name")
            }
            jobs {
                eq("project", projectName)

            }
        }

        return tagsByProject
    }
}
