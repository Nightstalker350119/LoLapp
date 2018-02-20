var myStyles = [
    {   width: 200,
        name: 'Blabla Sauce',
        color: '#268BD2'},
    {   width: 230,
        name: 'Teppan Yaki',
        color: '#BD3613'},
    {   width: 220,
        name: 'Sushi Sashimi',
        color: '#006600',},
    {   width: 290,
        name: 'Nigiri TaeKwonDo',
        color: '#ff00ff',},
    {   width: 236,
        name: 'John Salchichon',
        color: '#663300',},
    {   width: 230,
        name: 'Rambo Fields',
        color: '#6600cc'}
];

d3.selectAll('#chart').selectAll('div')
    .data(myStyles)
    .enter().append('div')
    .classed('item', true)
    .text(function(d) {
        return d.name;
    })
    .style({
        'color': 'white',
        'background' : function(d) {
            return d.color;
        },
        width : function(d) {
            return d.width + 'px';
        }
    })
