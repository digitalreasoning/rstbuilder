package com.digitalreasoning.rstwriter;

import java.util.Stack;

import com.digitalreasoning.rstwriter.bodyelement.LinkDefinition;
import com.digitalreasoning.rstwriter.bodyelement.Paragraph;

/**
 * The Heading class represents a reStructuredText Heading element. Headings can contain any body element, transitions,
 * or subheadings. Headings are the standard of content division in reStructuredText as they are in many kinds of text-based
 * content. ReStructuredText Headings also serve as implicit link targets in a table of contents or possibly inline 
 * references. Bordering and nesting of Headings is automatically handled. From highest level to lowest level, the bordering
 * standard is given by the following character order:
 *    # * = - ^ " ' : . / ; \ , ` [ { ( < + _ $ % & @ ? ! ] } ) >
 *
 * The first two will be over-lined as well, so a sample Heading tree:
 * Top Heading
 *
 *     Lower Heading
 *
 *         Third Heading
 *
 *             Fourth Heading
 *
 *         Same level as Third Heading
 *
 * would look like this:
 *
 * ###########
 * Top Heading
 * ###########
 *
 * *************
 * Lower Heading
 * *************
 *
 * Third Heading
 * =============
 *
 * Fourth Heading
 * --------------
 *
 * Same level as Third Heading
 * ===========================
 */
public class Heading implements RstElement {
    private ContentBase content;

    protected Heading(ContentBase content){
        this.content = content;
    }

    protected ContentBase getContentBase(){
        return content;
    }

    public static Builder builder(String name){
        return new Builder(name);
    }

    /**
     * Writes the content (the final reStructuredText) of this Heading to a single String object and returns it.
     * @return the reStructuredText generated by this Heading.
     */
    @Override
    public String write(){
        return content.write();
    }

    /**
     * The Builder class is used to instantiate a Heading object. The Builder takes all of the content to be included
     * in the Heading and stores it until {@code build()} is called. An instance of the Builder can be obtained via
     * the static {@code builder} method in Heading or by means of the public constructor. All additions of elements will
     * be seen in-order: that is, the order in which elements are added is the order they will appear in the Heading.
     * However, when using the {@code addLinkTarget} and {@code addDefinition} methods, the library's default behavior
     * will place link targets before the heading and definitions after all of the heading's content. To place these
     * elements in a specific place, use the {@code addBodyElement} method
     */
    public static class Builder{
        private ContentBase contentBase;
        private Stack<ContentBase> contentStack;

        /**
         * Public constructor for the Builder of a Heading.
         * @param name the desired title of the heading
         */
        public Builder(String name){
            contentBase = new ContentBase(name);
            contentStack = new Stack<>();
        }

        /**
         * Adds a paragraph to the content of this Builder, with optional {@link Inline} text support.
         * @param text The text of the paragraph
         * @param inlines optional inline markup options. See {@link Inline} for syntax help
         * @return this Builder with the paragraph added
         */
        public Builder addParagraph(String text, Inline... inlines){
            contentBase.add(new Paragraph(text, inlines));
            return this;
        }

        /**
         * Adds a body element to the content of this Builder
         * @param bodyElement the body element to be added (order of additions will be preserved in the Heading)
         * @return this Builder with the body element added
         */
        public Builder addBodyElement(RstBodyElement bodyElement){
            contentBase.add(bodyElement);
            return this;
        }

        /**
         * Adds a directive to the content of this Builder. Directives are body elements, so their order in the file
         * matches the order in which elements are added
         * @param directive the directive to be added
         * @return this Builder with the directive added
         */
        public Builder addDirective(Directive directive){
            contentBase.add(directive);
            return this;
        }

        /**
         * Link targets are special definitions that allow inline links to point to a specified place in the document. This
         * method places a link target above this Heading to allow inline references to this heading. In the text, it
         * will look like:
         * {@code .. target-name: 
         * 
         * Heading
         * =======}
         * @param name the defined name of the target
         * @return this Builder with the link target added
         */
        public Builder addLinkTarget(String name){
            contentBase.addLinkTarget(new LinkDefinition(name, ""));
            return this;
        }

        /**
         * Adds a definition to the content of this Builder. The definition will be seen after all other content in the Heading
         * @param definition the Definition to be added
         * @return this Builder with the definition added
         */
        public Builder addDefinition(Definition definition){
            contentBase.addDefinition(definition);
            return this;
        }

        /**
         * Adds a transition (a horizontal bar) to the content of this Builder. Transitions behave like body elements
         * in the RstFile's ordering of content
         * @return this Builder with a transition added
         */
        public Builder addTransition(){
            contentBase.add(new Transition());
            return this;
        }

        /**
         * Adds a subheading to the content of this Builder. Headings will be seen after body elements, in order of addition.
         * The level of the subheading will be one lower than this Builder's Heading.
         * @param heading the subheading to be added
         * @return this Builder with the subheading added
         */
        public Builder addSubHeading(Heading heading){
            contentBase.add(heading.getContentBase());
            return this;
        }

        /**
         * Begins building a subHeading to be placed inside this Heading. This method returns a new Heading Builder, so the
         * original builder can be kept and modified at the same time as the subheading. Only one subHeading may be open at a
         * time in a Builder. Once the subheading is complete, {@code closeSubHeading} is the appropriate method call to
         * return to the parent builder, not {@code build}.
         * @param name the title of the subheading
         * @return a new Builder for a subheading to be placed in this Builder.
         * @throws IllegalStateException if this Builder already has an open subheading
         */
        public Builder openSubHeading(String name){
            contentStack.push(contentBase);
            contentBase = new ContentBase(name);
            return this;
        }

        /**
         * Finishes building a subHeading, adds it to the parent Heading's content and returns the Builder to the parent
         * Heading.
         * @return the parent Builder with the subheading added to the parent's content
         * @throws IllegalStateException if this subheading doesn't have a parent
         */
        public Builder closeSubHeading(){
            if(contentStack.isEmpty()){
                throw new IllegalStateException("No subHeading was opened");
            }
            ContentBase parent = contentStack.pop();
            parent.add(contentBase);
            contentBase = parent;
            return this;
        }

        /**
         * Creates a Heading object initialized with the content provided to the Builder
         * @return an initialized, unmodifiable Heading object
         * @throws IllegalStateException if the builder still has an open subheading
         * @throws UnsupportedOperationException if called on a subheading that has a parent builder
         */
        public Heading build(){
            if(!contentStack.isEmpty()){
                throw new IllegalStateException("This builder has an open subheading.");
            }
            ContentBase copy = new ContentBase(contentBase);
            return new Heading(copy);
        }
    }
}
