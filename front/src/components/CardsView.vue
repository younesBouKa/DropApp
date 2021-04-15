<template>
    <div style="height: 100%">
        <el-row v-if="dataRows.length===0" class="empty-folder-capture">
            <el-col>
                <span>Empty folder</span>
            </el-col>
        </el-row>
        <el-row v-else
                v-for="row in dataRows"
                type="flex"
                :justify="row.length < nbr_cols_in_row ? 'start':'space-between'"
        >
                <el-col :span="6" v-for="node in row">
                    <div class="node-col">
                        <NodeCard
                                :node="node"
                                @click.native="selectNode(node)"
                                @dblclick.native="setCurrentNode(node)"
                                :class="selectedNodes.findIndex(n=>n.id===node.id)!==-1? 'selected_node':''"
                        >
                        </NodeCard>
                    </div>
                </el-col>
        </el-row>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    import NodeCard from '@/components/NodeCard.vue'

    const _ = require('lodash');
    export default {
        name: "CardsView",
        components: {
            NodeCard,
        },
        props: {
            data: {
                type: Array,
                default : ()=>[]
            }
        },
        data() {
            return {
                nbr_cols_in_row: 4,
                selectedNodes : [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
            currentNodeData(){
                return this.$store.getters.getCurrentNodeData;
            },
            dataRows() {
                return _.chunk(this.data, this.nbr_cols_in_row);
            },
            /* ...mapGetters({
                 selectedNode:"getCurrentNodeDataData"
             }),*/
        },
        watch: {
            dataRows(val){
                this.selectedNodes = [];
            }
        },
        mounted() {

        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            selectNode(node){
                console.log("selected node: ",node);
                let index = this.selectedNodes.findIndex(el=>el.id===node.id);
                if(index!==-1)
                    this.selectedNodes.splice(index,1);
                else
                    this.selectedNodes.push(node);
                this.$emit("selectionChanged",this.selectedNodes);
            },
            setCurrentNode(node){
                this.$store.commit("storeCurrentNodeData", node);
            }
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .empty-folder-capture{
        height: 100%;
        display: grid;
        justify-content: center;
        vertical-align: middle;
    }

    .selected_node >>> .el-card{
        background-color: #e3effd;
    }
</style>
