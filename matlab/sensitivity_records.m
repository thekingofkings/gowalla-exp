
files = ls('../data_sensitivity/distance-d30-u5000-*');
    
figure();
hold on;
box on;
grid on;
    
colors = [ [0, 0, 0.8]; [0.3, 0.6, 0.9]; ...
    [0, 100, 0] / 255; [0, 0.75, 0]; ...
    [255, 140, 0]/ 255; [255, 215, 0] / 255;  ...
    [153,50,204]/255; [216,191,216] /255];
color_ind = 1;
for ind = 1:size(files,1)
    
    data = importdata(['../data_sensitivity/', files(ind,:)]);
    [~, i] = sort(data(:,6));
    data = data(i, :);  

    size(data)

    pbg_locen = data(:,3);
    locen = data(:,4);
    pbg = data(:,5);
    freq = data(:,6);
    pbg_locen_td = data(:,7);
    td = data(:,8);
    friflag = data(:,9);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    

    precisionRecallPlot( freq, friflag, 'linestyle', '-', 'color', colors(color_ind,:) );
    color_ind = color_ind + 1;
%     precisionRecallPlot( pbg, friflag, 'r--' );
%     precisionRecallPlot( locen, friflag, 'linestyle', '--', 'color', [0, 0.75, 0] );
%     precisionRecallPlot( td, friflag, 'linestyle', '--', 'color', [255, 215, 0] / 255 );
%     precisionRecallPlot( pbg_locen, friflag, 'linestyle', '-', 'color', [0.3, 0.6, 0.9] );
    precisionRecallPlot( pbg_locen_td, friflag, 'linestyle', '-.', 'color', colors(color_ind,:) );
    color_ind = color_ind + 1;

end

%     axis([0,1,0.5,1]);
    hline = findobj(gcf, 'type', 'line');
    set(hline, 'linewidth', 3);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'Freq 20%', 'PGT 20%', 'Freq 50%', 'PGT 50%', ...
        'Freq 80%', 'PGT 80%', 'Freq 100%', 'PGT 100%'}, 'location', 'northeast');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print('sensitivity-recs.eps', '-dpsc');
    system('epstopdf sensitivity-recs.eps');
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);
