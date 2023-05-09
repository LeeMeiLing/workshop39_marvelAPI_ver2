export interface Character{
    // private Integer id;
    // private String name;
    // private String description;
    // private String resourceURI;
    // private String image;
    // private List<Comment> comments;

    id: string, // change from number to string
    name: string,
    description: string,
    resourceURI: string,
    image: string,
    comments: Comment[]
}

export interface Comment{
    // private Integer id;
    // private String charId;
    // private String comment;
    // private LocalDateTime timestamp;

    id: string,
    charId:string,
    comment: string,
    timestamp: Date

}