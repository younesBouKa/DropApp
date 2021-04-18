<template>
    <el-row class="content" @contextmenu.prevent.native="openMenu($event)">
        <ul v-show="false" :style="{'left':left+'px','top':top+'px'}" ref="contextMenu" id="contextMenu" class="context-menu">
            <li>Bring</li>
            <li>Send Backward</li>
        </ul>
    </el-row>
</template>

<script>
    const _ = require('lodash');
    export default {
        name: "ContextMenu",
        components :{
        },
        props: {
            folderId: {
                type: String,
                default: ()=> undefined
            },
        },
        data() {
            return {
                top : 0,
                left : 0,
                visible : false,
            }
        },
        computed: {
            currentNodeData(){
                return this.$store.getters.getCurrentNodeData;
            },
            isCurrentNodeFolder(){
               return this.currentNodeData.folder;
            },
            isFolderEmpty(){
               return this.isCurrentNodeFolder && this.data.length===0;
            },
            currentNodeElement(){
                return this.$store.getters.getCurrentNodeElement;
            },
            nodesInClipBoard(){
              return this.$store.getters.getNodesInClipBoard;
            },
        },
        watch: {
            currentNodeData(val) {

            },
            visible(value) {
                if (value) {
                    document.body.addEventListener('click', this.closeMenu)
                } else {
                    document.body.removeEventListener('click', this.closeMenu)
                }
            }
        },
        mounted() {
            let self = this;
            this.$bus.$on("files_uploaded", function (payLoad) {
                (payLoad||[]).forEach(node=>{

                })
            });

            this.$bus.$on("folder_created", function (payLoad) {

            });

            this.$bus.$on("nodes_deleted", function (payLoad) {
                (payLoad||[]).forEach(node=>{
                });
            });
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
                deleteNodesById: 'deleteNodesById',
            }),
            setCurrentFolder(folder) {
                console.log("setCurrentFolder: ",folder);
                if(folder.id){
                    this.$store.commit("storeCurrentNodeData", folder);
                }
            },
            openCreateFolderDialog(){
                this.$bus.$emit("create_folder",undefined);
            },
            openMenu(event) {
                console.log("open menu: ", event);
                const menuWidth = 100;
                const menuHeight = 100;
                console.log(`menu width: ${menuWidth} , height: ${menuHeight}`);
                const offsetLeft = this.$el.getBoundingClientRect().left // container margin left
                const offsetTop = this.$el.getBoundingClientRect().top // container margin top
                console.log(`container margin left: ${offsetLeft} , top: ${offsetTop}`);
                const offsetWidth = this.$el.offsetWidth // container width
                const offsetHeight = this.$el.offsetHeight // container height
                console.log(`container width: ${offsetWidth} , height: ${offsetHeight}`);
                const minLeft = offsetLeft;
                const minTop = offsetTop;
                const maxLeft = offsetLeft + offsetWidth - menuWidth - 20;
                const maxTop = offsetTop + offsetHeight - menuHeight - 20;
                console.log(`minLeft: ${minLeft} , minTop: ${minTop}, maxLeft: ${maxLeft}, maxTop: ${maxTop} `);
                const left = event.clientX
                const top = event.clientY

                if (left > maxLeft) {
                    this.left = maxLeft
                } else if(left < minLeft){
                    this.left = minLeft
                }else{
                    this.left = left;
                }

                if (top > maxTop) {
                    this.top = maxTop
                } else if(top < minTop) {
                    this.top = minTop
                } else {
                    this.top = top
                }
                this.top = this.top - window.pageYOffset;
                this.visible = true
            },
            closeMenu() {
                this.visible = false
            },
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .context-menu {
        position: fixed;
        background: white;
        z-index: 999;
        outline: none;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24);
        cursor: pointer;
    }
</style>
