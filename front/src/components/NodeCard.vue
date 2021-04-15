<template>
    <div class="block">
        <el-card shadow="hover">
            <el-row>
                <i :class="node.type==='FILE' ? 'el-icon-document' : 'el-icon-folder'"></i>
                <el-col>
                    <el-link :underline="false"
                             @click="setCurrentNodeData(node)"
                             style="cursor: pointer; margin-left: 10px;"
                             disable-transitions>
                        {{node.name}}
                    </el-link>
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
        display: flex;
        flex-direction: row;
    }
</style>
