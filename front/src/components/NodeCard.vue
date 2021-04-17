<template>
    <div class="block">
        <el-card shadow="hover">
            <el-row>
                <i :class="node.type==='FILE' ? 'el-icon-document' : 'el-icon-folder'"></i>
                <el-col style="margin-left: 5px; overflow: hidden;">
                    <el-row class="node-name-row">
                        <el-link :underline="false"
                                 @click="setCurrentNodeData(node)"
                                 style="cursor: pointer;"
                                 disable-transitions>
                            {{node.name}}
                        </el-link>
                    </el-row>
                    <el-row class="node-description-row">
                        <em class="node-path">{{node.path}}</em>
                    </el-row>
                </el-col>
            </el-row>
        </el-card>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'

    const _ = require('lodash');
    export default {
        name: "NodeCard",
        components: {},
        props: {
            node: Object
        },
        data() {
            return {
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
            /* ...mapGetters({
                 selectedNode:"getCurrentNodeDataData"
             }),*/
        },
        watch: {},
        mounted() {

        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            setCurrentNodeData(row) {
                this.$store.commit("storeCurrentNodeData", row);
            },
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .block {
        padding: 10px;
    }

    .el-card >>> .el-card__body{
        padding: 17px 5px 15px 10px;
        display: flex;
        justify-content: flex-start;
    }

    .el-link >>> i{
        font-size: 20px;
    }

    .el-row{
        overflow: hidden;
        display: flex;
        flex-direction: row;
    }

    .node-description-row{
        font-style: italic;
        font-size: x-small;
    }

    .node-name-row >>> a, .node-path{
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .el-link{
        display: unset;
        flex-direction: row;
        align-items: flex-start;
        justify-content: left;
    }

</style>
