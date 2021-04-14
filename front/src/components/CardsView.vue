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
                        <NodeCard :node="node"></NodeCard>
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
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
            selectedNode(){
                return this.$store.getters.getCurrentNode;
            },
            dataRows() {
                return _.chunk(this.data, this.nbr_cols_in_row);
            },
            /* ...mapGetters({
                 selectedNode:"getCurrentNode"
             }),*/
        },
        watch: {},
        mounted() {

        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            setCurrentNode(row) {
                this.$store.commit("storeCurrentNode", row);
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
    .empty-folder-capture{
        height: 100%;
        display: grid;
        justify-content: center;
        vertical-align: middle;
    }
</style>
