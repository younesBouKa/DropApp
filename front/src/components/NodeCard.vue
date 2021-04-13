<template>
    <div class="block">
        <el-card shadow="hover">
            {{node.name}}
        </el-card>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    const _ = require('lodash');
    export default {
        name: "NodeCard",
        components :{
        },
        props: {
            node: Object
        },
        data() {
            return {
                squareUrl : "https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png",
                filterText: "",
                data: [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
           /* ...mapGetters({
                selectedNode:"getCurrentNode"
            }),*/
        },
        watch: {

        },
        mounted() {

        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
