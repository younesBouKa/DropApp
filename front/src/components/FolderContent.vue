<template>
    <div style="height: 100%">
        <UploadFiles v-if="canUploadFileInFolder"></UploadFiles>
        <div v-else style="height: 100%;">
            <TableView :rows="data" :columns="columnsToShow"></TableView>
            <!--<CardsView :data="data"></CardsView>-->
        </div>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    import UploadFiles from '@/components/UploadFiles.vue'
    import CardsView from '@/components/CardsView.vue'
    import TableView from '@/components/TableView.vue'
    const _ = require('lodash');
    export default {
        name: "FolderContent",
        components :{
            UploadFiles,
            CardsView,
            TableView
        },
        props: {
            folderId: String,
            enableUpload : {
                type: Boolean,
                default : false
            }
        },
        data() {
            return {
                data: [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                },
                columnsToShow : [
                    {
                        field : "name",
                        label : "Name",
                    },
                    {
                        field : "createDate",
                        label : "Date de creation",
                    },
                    {
                        field : "permission",
                        label : "Permission",
                    },
                    "type"
                ]
            }
        },
        computed: {
            dataRows(){
                return _.chunk(this.data, this.nbr_cols_in_row);
            },
            selectedNode(){
                return this.$store.getters.getCurrentNode;
            },
            isSelectedNodeFolder(){
               return this.selectedNode.folder;
            },
            isFolderEmpty(){
               return this.isSelectedNodeFolder && this.data.length===0;
            },
            canUploadFileInFolder(){
                return this.isFolderEmpty
                    || this.enableUpload;
            },
           /* ...mapGetters({
                selectedNode:"getCurrentNode"
            }),*/
        },
        watch: {
            selectedNode(val) {
                this.loadFolderContent();
            }
        },
        mounted() {
            this.loadFolderContent();
            let self = this;
            this.$bus.$on("file_uploaded", function (payLoad) {
                self.loadFolderContent();
            });
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            loadFolderContent() {
                console.log("loadFolderContent : "+this.selectedNode);
                this.data = [];
                if (!this.selectedNode || !this.selectedNode.id || !this.selectedNode.folder)
                    return;
                this.getNodesByParentId(this.selectedNode.id)
                    .then(nodes => {
                        console.log(nodes);
                        this.data = nodes;
                    })
                    .catch(err => {
                        this.data = [];
                    });
            },
            openFullScreen2() {
                const loading = this.$loading({
                    lock: true,
                    text: 'Loading',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                setTimeout(() => {
                    loading.close();
                }, 2000);
            }
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
