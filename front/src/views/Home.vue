<template>
    <div class="home">
        <el-container style="height: 500px;">
            <el-aside>
                <SpaceTree></SpaceTree>
            </el-aside>
            <el-container style="margin-left: 5px;">
                <el-header >
                    <div class="header-buttons">
                        <el-avatar icon="el-icon-user-solid" size="small"></el-avatar>
                    </div>
                </el-header>

                <el-main>
                    <FolderContent :enableUpload="enableUpload"></FolderContent>
                </el-main>
            </el-container>
        </el-container>
        <CreateFolderDialog></CreateFolderDialog>
    </div>
</template>

<script>
    // @ is an alias to /src
    import UploadFiles from '@/components/UploadFiles.vue'
    import SpaceTree from '@/components/SpaceTree.vue'
    import FolderContent from '@/components/FolderContent.vue'
    import CreateFolderDialog from '@/components/CreateFolderDialog.vue'

    import { mapActions,mapGetters } from 'vuex'
    export default {
        name: 'Home',
        components: {
            UploadFiles,
            SpaceTree,
            FolderContent,
            CreateFolderDialog
        },
        data() {
            return {
                enableUpload : false,
            }
        },
        computed : {
            isSelectedNodeFolder(){
                return this.$store.getters.getCurrentNode.folder;
            },
            ...mapGetters([
                 "getCurrentNode"
                ]),
        },
        methods: {
            ...mapActions([
                'getNodeById',
                'getNodeByPath',
                'getNodesByParentPath',
                'getNodesByParentId',
            ]),
        },
        mounted() {
            let self = this;
            this.$bus.$on("file_uploaded", function (payLoad) {
                self.enableUpload = false;
            });
        }
    }
</script>

<style>
    .el-menu-item-group__title {
        padding-left: 40px;
        text-align: start;
    }

    .el-main {
        padding: 5px !important;
    }

    .el-header{
        display: flex;
        flex-direction: row;
        justify-content: flex-end;
        padding: 0 10px;
        line-height: 40px !important;
        height: 40px !important;
        text-align: right;
        font-size: 12px;
        color: #333;
        background-color: #eaedf2;
    }

    .header-buttons{
        display: flex;
        flex-direction: row;
        justify-content: flex-end;
        height: 28px;
        margin-top: 5px;
    }

    ::-webkit-scrollbar {
        width: 5px;
    }

    /* Track */
    ::-webkit-scrollbar-track {
        background: none;
    }

    /* Handle */
    ::-webkit-scrollbar-thumb {
        background: none;
    }

    /* Handle on hover */
    ::-webkit-scrollbar-thumb:hover {
        background: #cfc8c8;
        width: 10px;
    }

    .el-aside{
        color: #333;
        overflow: hidden !important;
        width: 200px !important;
        height: 100% !important;
    }

</style>